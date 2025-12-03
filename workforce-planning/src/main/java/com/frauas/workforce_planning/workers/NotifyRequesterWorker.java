package com.frauas.workforce_planning.workers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class NotifyRequesterWorker {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    public NotifyRequesterWorker(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @JobWorker(type = "notify-requester")
    public void notifyRequester(final JobClient client, final ActivatedJob job) {

        Map<String, Object> variables = job.getVariablesAsMap();

        String projectName = (String) variables.getOrDefault("projectName", "Unnamed Project");
        String requesterEmail = (String) variables.getOrDefault("requesterEmail", "default@example.com");

        Boolean dataValid = (Boolean) variables.get("dataValid");
        Boolean approved = (Boolean) variables.get("requestApproved");

        String subject;
        String body;

        // -------------------------------------
        // CASE 1 — REJECTED BY VALIDATION
        // -------------------------------------
        if (dataValid != null && !dataValid) {
            subject = "Staffing Request Rejected (Invalid Data)";
            body = String.format("""
                                 Your staffing request for project '%s' was rejected due to invalid or incomplete data.
                                 Please correct the fields and re-submit.
                                 
                                 Thank you.""",
                projectName
            );
        }
        // -------------------------------------
        // CASE 2 — REJECTED BY DEPARTMENT HEAD
        // -------------------------------------
        else if (approved != null && !approved) {
            subject = "Staffing Request Rejected by Department Head";
            body = String.format("""
                                 Your staffing request for project '%s' was reviewed and rejected by the Department Head.
                                 Please follow up with your approver for more details.
                                 
                                 Thank you.""",
                projectName
            );
        }
        // -------------------------------------
        // CASE 3 — APPROVED
        // -------------------------------------
        else {
            subject = "Staffing Request Approved";
            body = String.format("""
                                 Good news!
                                 
                                 Your staffing request for project '%s' has been approved.
                                 The Resource Planner will now work on employee assignment.
                                 
                                 Thank you.""",
                projectName
            );
        }

        // -------------------------------------
        // SEND EMAIL
        // -------------------------------------
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(requesterEmail);
            message.setSubject(subject);
            message.setText(body);

            emailSender.send(message);

            System.out.println("Email sent successfully to: " + requesterEmail);

        } catch (MailException e) {
            System.err.println("ERROR sending email: " + e.getMessage());
            // Optional: retry job
            // throw new RuntimeException(e);
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
