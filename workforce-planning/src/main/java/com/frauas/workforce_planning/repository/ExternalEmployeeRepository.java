package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.ExternalEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalEmployeeRepository extends JpaRepository<ExternalEmployee, Long> {

    Optional<ExternalEmployee> findByProviderAndExternalEmployeeId(
        String provider,
        String externalEmployeeId
    );

    List<ExternalEmployee> findByProjectId(Long projectId);

    List<ExternalEmployee> findByStaffingRequestId(Long staffingRequestId);
}
