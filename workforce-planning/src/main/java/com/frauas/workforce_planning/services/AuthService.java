package com.frauas.workforce_planning.services;

import com.frauas.workforce_planning.dto.*;
import com.frauas.workforce_planning.model.entity.User;
import com.frauas.workforce_planning.repository.UserRepository;
import com.frauas.workforce_planning.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.frauas.workforce_planning.services.AuthExceptions.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmailWithEmployeeAndRoles(req.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        List<String> roleNames = user.getRoles().stream().map(r -> r.getName()).toList();

        boolean allowed = roleNames.stream().anyMatch(r -> r.equalsIgnoreCase(req.portalRole()));
        if (!allowed) {
            throw new RoleNotAllowedException(req.portalRole());
        }

        // Safely handle null employee for external users
        String employeeHrId = (user.getEmployee() != null) ? user.getEmployee().getEmployeeId() : "EXTERNAL";
        Long empId = (user.getEmployee() != null) ? user.getEmployee().getId() : null;
        String firstName = (user.getEmployee() != null) ? user.getEmployee().getFirstName() : "External";
        String lastName = (user.getEmployee() != null) ? user.getEmployee().getLastName() : "User";

        String token = jwtService.generateToken(
                user.getEmail(),
                req.portalRole(),
                roleNames,
                employeeHrId
        );

        return new LoginResponse(
                token,
                user.getId(),
                empId,
                employeeHrId,
                firstName,
                lastName,
                req.portalRole(),
                roleNames
        );
    }

    public LoginResponse loginAuto(LoginAutoRequest req) {
        User user = userRepository.findByEmailWithEmployeeRolesAndDefaultRole(req.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        List<String> roleNames = user.getRoles().stream().map(r -> r.getName()).toList();

        // ✅ EXTERNAL user handling (employee is null)
        if (user.getEmployee() == null) {
            // For now: return token + placeholder identity; selectedRole left as EXTERNAL
            String selectedRole = "EXTERNAL"; // or null if you prefer
            String token = jwtService.generateToken(user.getEmail(), selectedRole, roleNames, "EXTERNAL");

            return new LoginResponse(
                    token,
                    user.getId(),
                    null,          // employeeDbId
                    "EXTERNAL",    // employeeHrId
                    "External",    // firstName placeholder
                    "User",        // lastName placeholder
                    selectedRole,
                    roleNames
            );
        }

        // ✅ INTERNAL user handling (same logic as you already wrote)

        // 1) Try default role name
        String defaultRoleName = user.getEmployee().getDefaultRole() != null
                ? user.getEmployee().getDefaultRole().getName()
                : null;

        // 2) Decide selected role WITHOUT reassigning the same variable
        final String selectedRole =
                (defaultRoleName != null) ? defaultRoleName
                : (roleNames.stream().anyMatch(r -> r.equalsIgnoreCase("EMPLOYEE"))) ? "EMPLOYEE"
                : (!roleNames.isEmpty()) ? roleNames.get(0)
                : null;

        if (selectedRole == null) {
            throw new RoleNotAllowedException("NO_ROLE_ASSIGNED");
        }

        // Safety: ensure role exists for that user
        // boolean allowed = roleNames.stream().anyMatch(r -> r.equalsIgnoreCase(selectedRole));
        // if (!allowed) {
        //     throw new RoleNotAllowedException(selectedRole);
        // }


            String employeeHrId = user.getEmployee().getEmployeeId();
            String token = jwtService.generateToken(user.getEmail(), selectedRole, roleNames, employeeHrId);

            return new LoginResponse(
                    token,
                    user.getId(),
                    user.getEmployee().getId(),
                    employeeHrId,
                    user.getEmployee().getFirstName(),
                    user.getEmployee().getLastName(),
                    selectedRole,
                    roleNames
            );
    }


}