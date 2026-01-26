package com.frauas.workforce_planning.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /** Central identity object carried through Spring Security */
    public record JwtPrincipal(
            Long userId,
            String email,
            String selectedRole
    ) {}

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Already authenticated → skip
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract identity
        Long userId = jwtService.extractUserId(token);
        String email = jwtService.extractEmail(token);
        String selectedRole = jwtService.extractSelectedRole(token);
        List<String> roles = jwtService.extractRoles(token);

        var authorities = roles.stream()
                .map(r -> r.startsWith("ROLE_")
                        ? new SimpleGrantedAuthority(r)
                        : new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .toList();

        JwtPrincipal principal = new JwtPrincipal(userId, email, selectedRole);

        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );

        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
