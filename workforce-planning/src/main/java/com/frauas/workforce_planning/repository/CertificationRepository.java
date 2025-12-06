package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    Optional<Certification> findByName(String name);
}
