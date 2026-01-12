package com.frauas.workforce_planning.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffingDecisionService {

  @Transactional
  public void reserve(Long requestId, Long employeeDbId) {
    // later: update EmployeeApplication.status = RESERVED
  }

  @Transactional
  public void assign(Long requestId, Long employeeDbId) {
    // later: update EmployeeApplication.status = ASSIGNED
    // later: subtract hours from Employee
  }
}
