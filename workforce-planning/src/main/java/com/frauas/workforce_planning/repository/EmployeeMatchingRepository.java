package com.frauas.workforce_planning.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeMatchingRepository {

  interface MatchResult {
    String getEmployeeId();        // employees.employee_id (EMP001...)
    Long getMatchedSkillCount();   // number of matched required skills
    Integer getScore();            // weighted score
  }

  @Query(value = """
    SELECT
      e.employee_id AS employeeId,
      COUNT(*) AS matchedSkillCount,
      (
        COUNT(*) * 10
        + SUM(CASE WHEN LOWER(COALESCE(es.experience_level,'')) = 'senior' THEN 3 ELSE 0 END)
        + SUM(CASE WHEN LOWER(COALESCE(es.experience_level,'')) = 'intermediate' THEN 2 ELSE 0 END)
        + SUM(CASE WHEN LOWER(COALESCE(es.experience_level,'')) = 'junior' THEN 1 ELSE 0 END)
      ) AS score
    FROM employees e
    JOIN employee_skills es ON es.employee_id = e.id
    JOIN skills s ON s.id = es.skill_id
    WHERE LOWER(s.name) IN (:skills)
    GROUP BY e.employee_id
    HAVING COUNT(*) >= :minMatches
    ORDER BY score DESC, matchedSkillCount DESC
  """, nativeQuery = true)
  List<MatchResult> findBestMatches(
      @Param("skills") List<String> skills,
      @Param("minMatches") int minMatches
  );
}
