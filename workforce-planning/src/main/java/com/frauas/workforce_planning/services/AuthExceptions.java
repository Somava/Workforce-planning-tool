package com.frauas.workforce_planning.services;

public class AuthExceptions {
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() { super("Invalid email or password"); }
    }
    public static class RoleNotAllowedException extends RuntimeException {
        public RoleNotAllowedException(String role) {
            super("This role is not available for you: " + role + ". Please log in with the correct portal.");
        }
    }
}
