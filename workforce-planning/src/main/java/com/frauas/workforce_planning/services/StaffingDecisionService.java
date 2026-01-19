package com.frauas.workforce_planning.services;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.dto.MatchedEmployeeDTO;
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.MatchingAvailability;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;

import io.camunda.zeebe.client.ZeebeClient;

@Service
public class StaffingDecisionService {

  private final StaffingRequestRepository staffingRequestRepository;
  private final EmployeeRepository employeeRepository;
  private final MatchingService matchingService;
  private final ZeebeClient zeebeClient;

  public StaffingDecisionService(
      StaffingRequestRepository staffingRequestRepository,
      EmployeeRepository employeeRepository,
      MatchingService matchingService,
      ZeebeClient zeebeClient
  ) {
    this.staffingRequestRepository = staffingRequestRepository;
    this.employeeRepository = employeeRepository;
    this.matchingService = matchingService;
    this.zeebeClient = zeebeClient;
  }

  @Transactional
  public void decide(Long requestId, boolean internalFound, Long employeeDbId) {

    StaffingRequest request = staffingRequestRepository.findByRequestId(requestId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "StaffingRequest not found: " + requestId
        ));

    if (internalFound) {
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
          .send()
          .join();

    } else {
      // Reset matched employees to AVAILABLE
      List<MatchedEmployeeDTO> matches = matchingService.matchEmployees(requestId, 1000);
      for (MatchedEmployeeDTO m : matches) {
        employeeRepository.findById(m.employeeDbId()).ifPresent(emp -> {
          emp.setMatchingAvailability(MatchingAvailability.AVAILABLE);
          employeeRepository.save(emp);
        });
      }

      request.setStatus(RequestStatus.EXTERNAL_SEARCH_TRIGGERED);
      staffingRequestRepository.save(request);

      zeebeClient.newPublishMessageCommand()
          .messageName("ResourcePlannerSelection")
          .correlationKey(requestId.toString())
          .variables(Map.of("suitableResourceFound", false))
          .send()
          .join();
    }
  } // ✅ THIS closes decide()

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
