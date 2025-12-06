package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.StaffingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffingRequestRepository extends JpaRepository<StaffingRequest, Long> {
}
