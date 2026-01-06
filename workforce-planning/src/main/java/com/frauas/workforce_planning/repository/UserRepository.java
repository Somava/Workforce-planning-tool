package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 1. The custom query (Ensure 'fetch u.externalEmployee' is removed here)
    @Query("select u from User u " +
           "left join fetch u.employee e " +
           "left join fetch u.roles r " +
           "where lower(u.email) = lower(:email)")
    Optional<User> findByEmailWithEmployeeAndRoles(@Param("email") String email);

    // 2. The derived query (Rename this to avoid the 'boolean' conflict)
    Optional<User> findByExternalEmployeeId(Long externalEmployeeId);
}