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

        // Safe fetch of ProjectDepartment mapping
        ProjectDepartment projDept = pdRepository.findByProject_IdAndDepartment_Id(project.getId(), dept.getId());

        // TALENT MAPPING (Internal vs External)
        String employeeEmail = null;
        String employeeName = "External Employee";
        String talentType = "INTERNAL";

        // --- PATH A: EXTERNAL CASE ---
        if (request.getStatus() == RequestStatus.EXT_EMPLOYEE_APPROVED_BY_DH) {
            talentType = "EXTERNAL";
            var externalData = externalEmployeeRepository.findByStaffingRequestId(requestId);
            if (externalData.isPresent()) {
                var ext = externalData.get();
                // Match the service logic: First Name + Last Name
                employeeName = ext.getFirstName() + " " + ext.getLastName();
            }
        } 
        // --- PATH B: INTERNAL CASE ---
        else if (request.getAssignedUser() != null) {
            User assignedUserAccount = request.getAssignedUser();
            employeeEmail = assignedUserAccount.getEmail();
            
            if (assignedUserAccount.getEmployee() != null) {
                Employee internalEmp = assignedUserAccount.getEmployee();
                // Match the service logic: First Name + Last Name
                employeeName = internalEmp.getFirstName() + " " + internalEmp.getLastName();
            }
        }

        // 3. Define the Summary
        String summary = """
                Project: %s
                Position: %s
                Assigned Talent: %s
                Talent Type: %s
                Requesting Manager: %s
                """.formatted(projectName, positionTitle, employeeName, talentType, managerName);

        // 4. Send Emails (with Null-Safety for DeptHead/Planner)
        sendEmail(managerEmail, "Staffing Complete: " + projectName, 
            "Hello " + managerName + ",\n\nAssignment is complete for your request. Please begin onboarding steps.\n\n" + summary);

        if (employeeEmail != null) {
            sendEmail(employeeEmail, "Congratulations! Your New Assignment", 
                "Hello " + employeeName + ",\n\nYou have been officially assigned. Please contact " + managerEmail + " to start.\n\n" + summary);
        }

        if (projDept != null) {
            if (projDept.getDepartmentHeadUser() != null) {
                sendEmail(projDept.getDepartmentHeadUser().getEmail(), "Resource Assignment Finalized", 
                    "The staffing request for your department has been filled.\n\n" + summary);
            }
            if (projDept.getResourcePlannerUser() != null) {
                sendEmail(projDept.getResourcePlannerUser().getEmail(), "Resource Allocation Finalized", 
                    "The staffing request is now closed and resource capacity updated.\n\n" + summary);
            }
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