package fr.zbar.showtime.backend.core.application;

public record AuthenticateUserCommand(String email, String password) {}
