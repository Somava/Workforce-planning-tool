package com.frauas.workforce_planning.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.RequestEntity;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {
    // JpaRepository gives you .save(), .findAll(), .delete() automatically!
}
