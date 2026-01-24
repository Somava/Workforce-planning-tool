package com.frauas.workforce_planning.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.MatchingAvailability;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;

import io.camunda.zeebe.client.ZeebeClient;

@Service
public class StaffingDecisionService {

    @Autowired
    private StaffingRequestRepository staffingRequestRepository;

    private final EmployeeRepository employeeRepository;

    @Autowired
    private ZeebeClient zeebeClient;

    public StaffingDecisionService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public void reserve(Long requestId, boolean internalFound, Long employeeDbId) {

        StaffingRequest request = staffingRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "StaffingRequest not found: " + requestId
                ));

        if (internalFound) {
            // --- INTERNAL RESERVATION LOGIC ---
            Employee employee = employeeRepository.findById(employeeDbId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Employee not found: " + employeeDbId
                    ));

            employee.setMatchingAvailability(MatchingAvailability.RESERVED);
            employeeRepository.save(employee);

            request.setStatus(RequestStatus.EMPLOYEE_RESERVED);
            if (employee.getUser() != null) {
                request.setAssignedUser(employee.getUser());
            }
            staffingRequestRepository.save(request);

            zeebeClient.newPublishMessageCommand()
                    .messageName("ResourcePlannerSelection")
                    .correlationKey(requestId.toString())
                    .variables(Map.of(
                            "suitableResourceFound", true,
                            "reservedEmployeeId", employeeDbId,
                            "isExternalCandidate", false
                    ))
                    .send().join();

        } else {
            // --- EXTERNAL TRIGGER (GROUP 3B) ---

            // 1. Cleanup existing internal assignments
            if (request.getAssignedUser() != null) {
                employeeRepository.findByUser_Id(request.getAssignedUser().getId())
                        .ifPresent(emp -> {
                            if (emp.getMatchingAvailability() == MatchingAvailability.RESERVED) {
                                emp.setMatchingAvailability(MatchingAvailability.AVAILABLE);
                                employeeRepository.save(emp);
                            }
                        });
                request.setAssignedUser(null);
            }

            // 2. Update Status to EXTERNAL_SEARCH_TRIGGERED
            request.setStatus(RequestStatus.EXTERNAL_SEARCH_TRIGGERED);
            staffingRequestRepository.save(request);

            // 3. Prepare full data payload for Group 3b
            Map<String, Object> group3bPayload = new HashMap<>();
            group3bPayload.put("suitableResourceFound", false);
            
            // Core Request Data
            group3bPayload.put("internalRequestId", request.getRequestId());
            group3bPayload.put("jobTitle", request.getTitle());
            group3bPayload.put("description", request.getDescription());
            group3bPayload.put("availabilityHoursPerWeek", request.getAvailabilityHoursPerWeek());
            group3bPayload.put("wagePerHour", request.getWagePerHour());
            group3bPayload.put("skills", request.getRequiredSkills());
            group3bPayload.put("experienceYears", request.getExperienceYears());
            group3bPayload.put("location", request.getWorkLocation()); // Remote/Onsite
            // group3bPayload.put("projectContext", request.getProjectContext());
            group3bPayload.put("startDate", request.getProjectStartDate().toString());
            group3bPayload.put("endDate", request.getProjectEndDate().toString());

            // Project Related Data (assuming StaffingRequest has a Project entity)
            if (request.getProject() != null) {
                group3bPayload.put("projectId", request.getProject().getId());
                group3bPayload.put("projectName", request.getProject().getName());
            }

            // 4. Publish Message to Zeebe
            zeebeClient.newPublishMessageCommand()
                    .messageName("ResourcePlannerSelection")
                    .correlationKey(requestId.toString())
                    .variables(group3bPayload)
                    .send()
                    .join();
            
            System.out.println("Handed off request to Group 3b: " + request.getTitle());
        }
    }

    @Transactional
    public void assign(Long requestId, Long employeeDbId) {
        Employee employee = employeeRepository.findById(employeeDbId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found: " + employeeDbId
                ));

        employee.setMatchingAvailability(MatchingAvailability.ASSIGNED);
        employeeRepository.save(employee);
    }

    public void receiveExternalCandidate(String requestId, Map<String, Object> candidateData) {
    // 1. Log the receipt for debugging
        System.out.println("Received candidate " + candidateData.get("name") + " for Request: " + requestId);

        // 2. Resume the BPMN Flow
        // The variables here will be used by the "External Resource Found?" gateway in your diagram
        zeebeClient.newPublishMessageCommand()
                .messageName("ExternalCandidateReceived") // Matches the Catch Event name
                .correlationKey(requestId)                 // Matches internalRequestId
                .variables(Map.of(
                    "externalResourceFound", true,         // Moves flow to 'Found' path
                    "externalCandidateName", candidateData.get("name"),
                    "externalCandidateWage", candidateData.get("wage")
                ))
                .send()
                .join();
    }
}