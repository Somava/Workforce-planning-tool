package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeMatchingRepository extends JpaRepository<Employee, Long> {

    interface MatchResult {
        String getEmployeeId();        // employees.employee_id
        Long getMatchedSkillCount();   // number of matched required skills
        Integer getScore();            // weighted score based on JSONB data
    }

    /**
     * 🔹 Matches employees by comparing JSONB skills.
     * Assumes employee.skills format: [{"name": "Java", "level": "Senior"}, ...]
     */
    @Query(value = """
        WITH expanded_skills AS (
            SELECT 
                e.id, 
                e.employee_id,
                skill_elem->>'name' as skill_name,
                skill_elem->>'level' as experience_level
            FROM employees e,
            jsonb_array_elements(e.skills) AS skill_elem
        )
        SELECT 
            employee_id AS employeeId,
            COUNT(*) AS matchedSkillCount,
            (
                COUNT(*) * 10 
                + SUM(CASE WHEN LOWER(experience_level) = 'senior' THEN 3 ELSE 0 END)
                + SUM(CASE WHEN LOWER(experience_level) = 'intermediate' THEN 2 ELSE 0 END)
                + SUM(CASE WHEN LOWER(experience_level) = 'junior' THEN 1 ELSE 0 END)
            ) AS score
        FROM expanded_skills
        WHERE LOWER(skill_name) IN (:skills)
        GROUP BY id, employee_id
        HAVING COUNT(*) >= :minMatches
        ORDER BY score DESC, matchedSkillCount DESC
    """, nativeQuery = true)
    List<MatchResult> findBestMatches(
        @Param("skills") List<String> skills,
        @Param("minMatches") int minMatches
    );
}