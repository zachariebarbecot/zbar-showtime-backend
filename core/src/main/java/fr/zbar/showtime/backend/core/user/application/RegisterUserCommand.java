package fr.zbar.showtime.backend.core.user.application;

import java.time.LocalDate;

public record RegisterUserCommand(
    String lastName,
    String firstName,
    String email,
    String password,
    LocalDate birthDate,
    String gender) {}
