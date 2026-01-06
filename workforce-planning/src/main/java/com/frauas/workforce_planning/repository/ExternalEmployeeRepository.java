package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.ExternalEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalEmployeeRepository extends JpaRepository<ExternalEmployee, Long> {

    Optional<ExternalEmployee> findByExternalEmployeeId(String externalEmployeeId);

    List<ExternalEmployee> findByProvider(String provider);

    // This traverses ExternalEmployee -> StaffingRequest -> requestId
    List<ExternalEmployee> findByStaffingRequest_RequestId(Long requestId);

    @Query(value = "SELECT * FROM external_employees WHERE skills @> CAST(:skillsJson AS jsonb)", nativeQuery = true)
    List<ExternalEmployee> findBySkills(@Param("skillsJson") String skillsJson);
}