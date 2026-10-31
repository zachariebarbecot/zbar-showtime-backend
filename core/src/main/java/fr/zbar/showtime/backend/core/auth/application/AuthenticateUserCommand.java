package fr.zbar.showtime.backend.core.auth.application;

public record AuthenticateUserCommand(String email, String password) {}
