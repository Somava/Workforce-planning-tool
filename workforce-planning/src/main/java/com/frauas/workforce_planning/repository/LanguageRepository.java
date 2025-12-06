package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Long> {
    Optional<Language> findByName(String name);
}
