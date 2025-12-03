package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}
