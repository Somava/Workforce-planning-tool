package com.frauas.workforce_planning.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.enums.MatchingAvailability;
import com.frauas.workforce_planning.repository.EmployeeRepository;

@Service
public class StaffingDecisionService {

  private final EmployeeRepository employeeRepository;

  public StaffingDecisionService(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  @Transactional
  public void reserve(Long requestId, Long employeeDbId) {
    Employee employee = employeeRepository.findById(employeeDbId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Employee not found: " + employeeDbId
        ));

    employee.setMatchingAvailability(MatchingAvailability.RESERVED);
    employeeRepository.save(employee);
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
