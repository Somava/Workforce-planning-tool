package com.frauas.workforce_planning.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.RequestEntity;
import com.frauas.workforce_planning.model.enums.RequestStatus;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {
    // This allows you to find the record using your custom generated ID
    Optional<RequestEntity> findByRequestId(Long requestId);
    List<RequestEntity> findAllByStatus(RequestStatus status);
}
