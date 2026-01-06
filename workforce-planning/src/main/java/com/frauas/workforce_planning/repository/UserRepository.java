package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 🔹 Updated to fetch both possible account types (Internal or External)
    @Query("""
        select u from User u
        left join fetch u.employee e
        left join fetch u.externalEmployee ex
        left join fetch u.roles r
        where lower(u.email) = lower(:email)
    """)
    Optional<User> findByEmailWithEmployeeAndRoles(@Param("email") String email);

    // 🔹 Standard lookup by email
    Optional<User> findByEmailIgnoreCase(String email);

    // 🔹 Lookup for users specifically linked to an internal employee
    Optional<User> findByEmployee_Id(Long employeeId);

    // 🔹 New: Lookup for users specifically linked to an external provider employee
    Optional<User> findByExternalEmployee_Id(Long externalEmployeeId);
}