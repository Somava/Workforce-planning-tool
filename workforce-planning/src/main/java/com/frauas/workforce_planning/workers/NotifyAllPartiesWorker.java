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
public class NotifyAllPartiesWorker {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    public NotifyAllPartiesWorker(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    // Set the JobWorker type to match the BPMN task's job type, e.g., "notify-all-parties"
    @JobWorker(type = "notify-all-parties")
    public void notifyAllParties(final JobClient client, final ActivatedJob job) {

        Map<String, Object> variables = job.getVariablesAsMap();

        // 1. Get required variables from the process
        String projectName = (String) variables.getOrDefault("projectName", "Unnamed Project");
        // These variables MUST be set earlier in the process instance!
        String requesterEmail = (String) variables.getOrDefault("requesterEmail", "requester-missing@example.com");
        String assignedEmployeeEmail = (String) variables.getOrDefault("assignedEmployeeEmail", "employee-missing@example.com");
        String assignedEmployeeName = (String) variables.getOrDefault("assignedEmployeeName", "A Resource");
        String departmentHeadEmail = (String) variables.getOrDefault("departmentHeadEmail", "head-missing@example.com");


        // 2. Define email content for Requester (Manager)
        String requesterSubject = "Staffing Request Completed: Project " + projectName;
        String requesterBody = String.format("""
                The staffing request for project '%s' has been successfully completed and approved.
                
                The assigned employee is **%s**.
                
                Thank you for using the workforce planning system.""",
                projectName,
                assignedEmployeeName
        );

        // 3. Define email content for Assigned Employee
        String employeeSubject = "New Staffing Assignment: Project " + projectName;
        String employeeBody = String.format("""
                Congratulations! You have been successfully assigned to the following project:
                
                **Project:** %s
                **Project Requester Email:** %s
                
                Please contact the requester to begin your assignment and for further details.
                
                Thank you.""",
                projectName,
                requesterEmail
        );
        
        // 4. Define email content for Department Head (or Resource Planner, for informational purposes)
        String headSubject = "Project Staffed Confirmation: Project " + projectName;
        String headBody = String.format("""
                The staffing request for project '%s' has been fully processed and completed.
                
                **Assigned Employee:** %s
                
                This is for your information.""",
                projectName,
                assignedEmployeeName
        );


        // 5. Send Emails to all parties
        sendEmail(requesterEmail, requesterSubject, requesterBody);
        sendEmail(assignedEmployeeEmail, employeeSubject, employeeBody);
        sendEmail(departmentHeadEmail, headSubject, headBody);


        // 6. Complete the Job and set process variable
        client.newCompleteCommand(job.getKey())
                .variable("finalNotificationSent", true)
                .send()
                .join();
    }

    /**
     * Helper method to construct and send a single email.
     * @param toEmail The recipient's email address.
     * @param subject The email subject line.
     * @param body The email body text.
     */
    private void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            emailSender.send(message);
            System.out.println("Final notification email sent successfully to: " + toEmail);

        } catch (MailException e) {
            System.err.println("ERROR sending final email to " + toEmail + ": " + e.getMessage());
            
        }
    }
}