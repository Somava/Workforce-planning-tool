package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // 🔹 Find a department by its unique name (e.g., "IT", "HR")
    Optional<Department> findByName(String name);

    // 🔹 Find the department managed by a specific User ID
    // This matches your 'departmentHead' field in Department.java
    Optional<Department> findByDepartmentHeadId(Long userId);
}
