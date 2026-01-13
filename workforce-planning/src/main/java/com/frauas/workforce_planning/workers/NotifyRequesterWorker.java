package com.frauas.workforce_planning.workers;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotifyRequesterWorker {

    private final JavaMailSender emailSender;
    private final StaffingRequestRepository staffingRequestRepository;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    public NotifyRequesterWorker(JavaMailSender emailSender, StaffingRequestRepository staffingRequestRepository) {
        this.emailSender = emailSender;
        this.staffingRequestRepository = staffingRequestRepository;
    }

    @JobWorker(type = "notify-requester")
    public void notifyRequester(final JobClient client, final ActivatedJob job) {

        Map<String, Object> variables = job.getVariablesAsMap();

        // 1. Identify the Request from Process Variables
        // We assume 'requestId' was passed when the process started or task completed
        Long requestId = null;
        if (variables.get("requestId") != null) {
            requestId = Long.valueOf(variables.get("requestId").toString());
        }

        String projectName = (String) variables.getOrDefault("projectName", "Unnamed Project");
        String requesterEmail = (String) variables.getOrDefault("requesterEmail", "default@example.com");
        String requesterName = "Manager";

        if (requestId != null) {
            Optional<StaffingRequest> requestOpt = staffingRequestRepository.findById(requestId);
            if (requestOpt.isPresent()) {
                StaffingRequest request = requestOpt.get();
                projectName = request.getTitle(); // Get title directly from DB for accuracy
                
                // Fetch the Manager entity (the person who created the request)
                Employee creator = request.getCreatedBy();
                if (creator != null) {
                    requesterEmail = creator.getEmail();
                    requesterName = creator.getFirstName() + " " + creator.getLastName();
                }
            }
        }

        Boolean dataValid = (Boolean) variables.get("dataValid");
        Boolean approved = (Boolean) variables.get("deptHeadApproved");
        // 1. PULL THE REASON FROM VARIABLES
String rejectionReason = (String) variables.getOrDefault("rejectionReason", "No specific reason provided.");
        String subject;
        String body;

        // -------------------------------------
        // CASE 1 — REJECTED BY VALIDATION
        // -------------------------------------
        if (dataValid != null && !dataValid) {
            subject = "Staffing Request Rejected (Invalid Data)";
            body = String.format("""
                                 Dear %s, 
                                 Your staffing request for project '%s' was rejected due to invalid or incomplete data.
                                 Please correct the fields and re-submit.
                                 
                                 Thank you.""",
                requesterName,projectName
            );
        }
        // -------------------------------------
        // CASE 2 — REJECTED BY DEPARTMENT HEAD
        // -------------------------------------
        else if (approved != null && !approved) {
            subject = "Staffing Request Rejected by Department Head";
            body = String.format("""
                                 Dear %s, 
                                 Your staffing request for project '%s' was reviewed and rejected by the Department Head.
                                 Reason for Rejection:
                                 "%s"
                                 
                                 Please follow up with your approver for more details.
                                 
                                 Thank you.""",
                requesterName, projectName, rejectionReason
            );
        }
        // -------------------------------------
        // CASE 3 — APPROVED
        // -------------------------------------
        else {
            subject = "Staffing Request Approved";
            body = String.format("""
                                 Dear %s, 
                                 Good news!
                                 
                                 Your staffing request for project '%s' has been approved.
                                 The Resource Planner will now work on employee assignment.
                                 
                                 Thank you.""",
                requesterName,projectName
            );
        }

        // -------------------------------------
        // SEND EMAIL
        // -------------------------------------
        try {
    // Check if email is null before trying to use it
    if (requesterEmail == null || requesterEmail.trim().isEmpty()) {
        log.error("ABORTING EMAIL: No recipient email address found for Request ID {}", requestId);
    } else {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(requesterEmail);
        message.setSubject(subject);
        message.setText(body);

        emailSender.send(message);
        log.info("Email sent successfully to Manager: {} ({})", requesterName, requesterEmail);
    }

} catch (MailException e) {
    log.error("ERROR sending email to {}: {}", requesterEmail, e.getMessage());
}

        // -------------------------------------
        // COMPLETE JOB
        // -------------------------------------
        client.newCompleteCommand(job.getKey())
                .variable("requesterNotified", true)
                .send()
                .join();
    }
}
