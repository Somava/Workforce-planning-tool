package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
