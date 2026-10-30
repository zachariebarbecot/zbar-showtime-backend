package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.domain.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String lastName,
    String firstName,
    String email,
    LocalDate birthDate,
    String gender,
    Instant createdAt,
    Instant updatedAt) {

  static UserResponse from(User user) {
    return new UserResponse(
        user.id(),
        user.name().lastName(),
        user.name().firstName(),
        user.email().value(),
        user.birthDate().value(),
        user.gender().name(),
        user.createdAt(),
        user.updatedAt());
  }
}
