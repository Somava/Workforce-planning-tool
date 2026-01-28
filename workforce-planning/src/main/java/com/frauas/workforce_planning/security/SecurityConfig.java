package com.frauas.workforce_planning.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Enable CORS (uses your WebConfig.java settings)
                .cors(Customizer.withDefaults())
                
                // 2. Disable CSRF for stateless REST APIs
                .csrf(csrf -> csrf.disable()) 
                
                // 3. Set Session Policy to Stateless (JWT)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 4. Configure Endpoint Permissions
                .authorizeHttpRequests(auth -> auth
                        // Public: Authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll() 
                        
                        // Public: Swagger UI & OpenAPI Documentation
                        .requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/v3/api-docs.yaml",
                            "/swagger-resources/**",
                            "/webjars/**"
                        ).permitAll()
                        
                        // Public: Team 3B Integration endpoints
                        .requestMatchers("/api/integration/**", "/api/group3b/**").permitAll()
                        
                        // Secure: Everything else requires a valid JWT
                        .anyRequest().authenticated()
                )
                
                // 5. Add JWT Filter before the standard UsernamePassword filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}