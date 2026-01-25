package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.enums.MatchingAvailability;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
@Query(value = """
    select l.name as name, el.proficiency_level as proficiency
    from employee_languages el
    join languages l on l.id = el.language_id
    where el.employee_id = :employeeId
    order by l.id
""", nativeQuery = true)
List<Object[]> findLanguagesWithProficiency(@Param("employeeId") Long employeeId);


    // 🔹 Find by the company HR ID
    Optional<Employee> findByEmployeeId(String employeeId);

    // 🔹 Search by the new normalized Department entity
    // This uses JPA's underscore notation to traverse the relationship to Department.name
    List<Employee> findByDepartment_Name(String departmentName);

    // 🔹 Find by the Department ID directly
    List<Employee> findByDepartment_Id(Long departmentId);

    // 🔹 Support for the new capacity tracking fields in the new schema
    List<Employee> findByRemainingHoursPerWeekGreaterThanEqual(Integer hours);
    
    // 🔹 Find employee linked to a given User (used for undoing reserve)
       Optional<Employee> findByUser_Id(Long userId);





    // 🔹 Find by Email for the Employee Portal lookups
   Optional<Employee> findByEmail(String email);

    // 🔹 PostgreSQL Native Query to search within the JSONB skills column
    // The '@>' operator checks if the JSON on the left contains the JSON on the right
    @Query(value = """
        SELECT * FROM employees 
        WHERE skills @> CAST(:skillsJson AS jsonb)
        """, nativeQuery = true)
    List<Employee> findBySkills(@Param("skillsJson") String skillsJson);

    // 🔹 Find employees belonging to any of the departments in the provided list
List<Employee> findByDepartment_IdIn(List<Long> departmentIds);

    

     // search with matching availability filters
    List<Employee> findByMatchingAvailability(MatchingAvailability availability);

    List<Employee> findByMatchingAvailabilityAndRemainingHoursPerWeekGreaterThanEqual(
            MatchingAvailability availability,
            Integer hours
    );
}