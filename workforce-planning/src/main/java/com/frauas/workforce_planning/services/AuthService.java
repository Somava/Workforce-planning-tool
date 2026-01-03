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

        String employeeHrId = user.getEmployee().getEmployeeId();

        String token = jwtService.generateToken(
                user.getEmail(),
                req.portalRole(),
                roleNames,
                employeeHrId
        );

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmployee().getId(),
                employeeHrId,
                user.getEmployee().getFirstName(),
                user.getEmployee().getLastName(),
                req.portalRole(),
                roleNames
        );
    }
}
