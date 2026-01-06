package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 🔹 Find by the company HR ID
    Optional<Employee> findByEmployeeId(String employeeId);

    // 🔹 Search by the new normalized Department entity
    // This uses JPA's underscore notation to traverse the relationship to Department.name
    List<Employee> findByDepartment_Name(String departmentName);

    // 🔹 Find by the Department ID directly
    List<Employee> findByDepartment_Id(Long departmentId);

    // 🔹 Support for the new capacity tracking fields in the new schema
    List<Employee> findByRemainingHoursPerWeekGreaterThanEqual(Integer hours);

    // 🔹 PostgreSQL Native Query to search within the JSONB skills column
    // The '@>' operator checks if the JSON on the left contains the JSON on the right
    @Query(value = """
        SELECT * FROM employees 
        WHERE skills @> CAST(:skillsJson AS jsonb)
        """, nativeQuery = true)
    List<Employee> findBySkills(@Param("skillsJson") String skillsJson);
    
    // 🔹 Search for employees with specific job roles
    List<Employee> findByJobRole_Id(Long jobRoleId);
}