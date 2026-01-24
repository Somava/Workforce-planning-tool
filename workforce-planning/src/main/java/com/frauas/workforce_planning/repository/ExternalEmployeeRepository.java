package com.frauas.workforce_planning.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.ExternalEmployee;

@Repository
public interface ExternalEmployeeRepository extends JpaRepository<ExternalEmployee, Long> {
    // The part after 'findBy' MUST match the variable name above exactly
    Optional<ExternalEmployee> findByExternalEmployeeId(String externalEmployeeId);

    Optional<ExternalEmployee> findByStaffingRequestId(Long staffingRequestId);
}