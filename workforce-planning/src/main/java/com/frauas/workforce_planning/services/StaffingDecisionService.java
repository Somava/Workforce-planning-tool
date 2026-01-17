package com.frauas.workforce_planning.services;

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

  @Autowired
  private final EmployeeRepository employeeRepository;

  @Autowired
  private ZeebeClient zeebeClient;

  public StaffingDecisionService(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  @Transactional
  public void reserve(Long requestId, Long employeeDbId) {

    // Load request
    StaffingRequest request = staffingRequestRepository.findByRequestId(requestId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "StaffingRequest not found: " + requestId
        ));

    // Load employee
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
            "reservedEmployeeId", employeeDbId // optional but very useful downstream
        ))
        .send()
        .join();

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
}
