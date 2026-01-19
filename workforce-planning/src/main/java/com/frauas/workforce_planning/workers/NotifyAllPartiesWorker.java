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
import org.springframework.transaction.annotation.Transactional;

import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.entity.Department;
import com.frauas.workforce_planning.model.entity.User;
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotifyAllPartiesWorker {

    private final JavaMailSender emailSender;
    private final StaffingRequestRepository repository;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    public NotifyAllPartiesWorker(JavaMailSender emailSender, StaffingRequestRepository repository) {
        this.emailSender = emailSender;
        this.repository = repository;
    }

    @JobWorker(type = "notify-all-parties")
    @Transactional
    public void notifyAllParties(final JobClient client, final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        Long requestId = ((Number) variables.get("requestId")).longValue();

        // 1. Fetch fresh data from DB
        StaffingRequest request = repository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found for Worker: " + requestId));

        Department dept = request.getDepartment();

        // 2. Extract Data
        String projectName = request.getProjectName();
        String positionTitle = request.getTitle();
        
        // MANAGER (The Requester - StaffingRequest.getCreatedBy() returns Employee)
        Employee creatorEmployee = request.getCreatedBy();
        String managerEmail = (creatorEmployee.getUser() != null) ? creatorEmployee.getUser().getEmail() : "N/A";
        String managerName = creatorEmployee.getFirstName() + " " + creatorEmployee.getLastName();

        // DEPARTMENT HEAD
        String deptHeadEmail = (dept.getDepartmentHead() != null) ? dept.getDepartmentHead().getEmail() : null;

        // RESOURCE PLANNER
        String plannerEmail = (dept.getResourcePlanner() != null) ? dept.getResourcePlanner().getEmail() : null;

        // EMPLOYEE (The Assigned Talent - StaffingRequest.getAssignedUser() returns User)
        String employeeEmail = null;
        String employeeName = "External/Freelancer";
        if (request.getAssignedUser() != null) {
            User assignedUserAccount = request.getAssignedUser();
            employeeEmail = assignedUserAccount.getEmail();
            
            // Navigate User -> Employee to get the names
            if (assignedUserAccount.getEmployee() != null) {
                employeeName = assignedUserAccount.getEmployee().getFirstName() + " " + 
                               assignedUserAccount.getEmployee().getLastName();
            }
        }

        // 3. Define the Summary
        String summary = """
                Project: %s
                Position: %s
                Assigned Employee: %s
                Requesting Manager: %s
                """.formatted(projectName, positionTitle, employeeName, managerName);

        // 4. Send Emails
        
        // Notify Manager
        sendEmail(managerEmail, "Staffing Complete: " + projectName, 
            "Hello " + managerName + ",\n\nAssignment is complete. Please begin onboarding steps.\n\n" + summary);

        // Notify Employee
        if (employeeEmail != null) {
            sendEmail(employeeEmail, "Congratulations! Your New Assignment", 
                "Hello " + employeeName + ",\n\nYou have been officially assigned. Please contact " + managerEmail + " to start.\n\n" + summary);
        }

        // Notify Department Head
        if (deptHeadEmail != null) {
            sendEmail(deptHeadEmail, "Resource Assignment Finalized", 
                "The staffing request for your department has been filled.\n\n" + summary);
        }

        // Notify Resource Planner
        if (plannerEmail != null) {
            sendEmail(plannerEmail, "Resource Allocation Finalized", 
                "The staffing request is now closed and resource capacity updated.\n\n" + summary);
        }

        // 5. Complete Job in Camunda
        client.newCompleteCommand(job.getKey())
                .variable("finalNotificationSent", true)
                .send()
                .join();
    }

    private void sendEmail(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isEmpty() || toEmail.equals("N/A")) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            emailSender.send(message);
            log.info("Notification sent to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to notify {}: {}", toEmail, e.getMessage());
        }
    }
}