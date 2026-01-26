package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
        select distinct u from User u
        join fetch u.employee e
        left join fetch u.roles r
        where lower(u.email) = lower(:email)
    """)
    Optional<User> findByEmailWithEmployeeAndRoles(@Param("email") String email);

    @Query("""
        select distinct u from User u
        left  join fetch u.employee e
        left join fetch e.defaultRole dr
        left join fetch u.roles r
        where lower(u.email) = lower(:email)
    """)
    Optional<User> findByEmailWithEmployeeRolesAndDefaultRole(@Param("email") String email);

    Optional<User> findByExternalEmployeeId(Long externalEmployeeId);
    Optional<User> findByEmail(String email);
        @Query(value = """
        SELECT u.employee_id
        FROM users u
        JOIN user_roles ur ON ur.user_id = u.id
        JOIN roles r ON r.id = ur.role_id
        WHERE r.name IN ('ROLE_MANAGER','ROLE_DEPT_HEAD','ROLE_RESOURCE_PLNR')
          AND u.employee_id IS NOT NULL
        """, nativeQuery = true)
    Set<Long> findLeadershipEmployeeIds();

    // Find the Department Head (e.g., Bob)
    @Query("""
        SELECT u FROM User u 
        JOIN u.roles r 
        WHERE r.name = 'ROLE_DEPT_HEAD' 
        AND u.employee.department.id = :deptId
    """)
    Optional<User> findDepartmentHeadByDeptId(@Param("deptId") Long deptId);

    // Find the Resource Planner
    @Query("""
        SELECT u FROM User u 
        JOIN u.roles r 
        WHERE r.name = 'ROLE_RESOURCE_PLNR' 
        AND u.employee.department.id = :deptId
    """)
    Optional<User> findResourcePlannerByDeptId(@Param("deptId") Long deptId);

}
