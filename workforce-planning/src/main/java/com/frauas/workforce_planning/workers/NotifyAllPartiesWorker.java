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
import com.frauas.workforce_planning.model.entity.Project;
import com.frauas.workforce_planning.model.entity.ProjectDepartment;
import com.frauas.workforce_planning.repository.ProjectDepartmentRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.repository.ExternalEmployeeRepository; 
import com.frauas.workforce_planning.model.enums.RequestStatus;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotifyAllPartiesWorker {

    private final JavaMailSender emailSender;
    private final StaffingRequestRepository repository;

    @Autowired
    private ProjectDepartmentRepository pdRepository;

    @Autowired
    private ExternalEmployeeRepository externalEmployeeRepository;

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
        Project project = request.getProject();

        // 2. Extract Data
        String projectName = request.getProjectName();
        String positionTitle = request.getTitle();
        
        // MANAGER (The Requester)
        Employee creatorEmployee = request.getCreatedBy();
        String managerEmail = (creatorEmployee.getUser() != null) ? creatorEmployee.getUser().getEmail() : "N/A";
        String managerName = creatorEmployee.getFirstName() + " " + creatorEmployee.getLastName();

        ProjectDepartment projDept = pdRepository.findByProject_IdAndDepartment_Id(project.getId(), dept.getId());

        // DEPARTMENT HEAD
        String deptHeadEmail = (projDept.getDepartmentHeadUser() != null) ? projDept.getDepartmentHeadUser().getEmail() : null;

        // RESOURCE PLANNER
        String plannerEmail = (projDept.getResourcePlannerUser() != null) ? projDept.getResourcePlannerUser().getEmail() : null;

        // TALENT MAPPING (Internal vs External)
        String employeeEmail = null;
        String employeeName = "External Employee";

        // Handle External Case
        if (request.getStatus() == RequestStatus.EXT_EMPLOYEE_APPROVED_BY_DH) {
            employeeName = externalEmployeeRepository.findByStaffingRequestId(requestId)
                    .map(ee -> ee.getFirstName() + " " + ee.getLastName())
                    .orElse("External Employee");
            // External employees typically don't have a system email yet
            employeeEmail = null; 
        } 
        // Handle Internal Case (Your existing logic)
        else if (request.getAssignedUser() != null) {
            User assignedUserAccount = request.getAssignedUser();
            employeeEmail = assignedUserAccount.getEmail();
            
            if (assignedUserAccount.getEmployee() != null) {
                employeeName = assignedUserAccount.getEmployee().getFirstName() + " " + 
                               assignedUserAccount.getEmployee().getLastName();
            }
        } else {
            employeeName = "External/Freelancer";
        }

        // 3. Define the Summary
        String summary = """
                Project: %s
                Position: %s
                Assigned Talent: %s
                Requesting Manager: %s
                """.formatted(projectName, positionTitle, employeeName, managerName);

        // 4. Send Emails
        
        // Notify Manager
        sendEmail(managerEmail, "Staffing Complete: " + projectName, 
            "Hello " + managerName + ",\n\nAssignment is complete. Please begin onboarding steps.\n\n" + summary);

        // Notify Employee (Only if internal)
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