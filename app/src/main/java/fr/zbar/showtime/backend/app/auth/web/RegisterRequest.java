package fr.zbar.showtime.backend.app.auth.web;

import fr.zbar.showtime.backend.core.user.application.RegisterUserCommand;
import java.time.LocalDate;

public record RegisterRequest(
    String lastName,
    String firstName,
    String email,
    String password,
    LocalDate birthDate,
    String gender) {

  RegisterUserCommand toCommand() {
    return new RegisterUserCommand(lastName, firstName, email, password, birthDate, gender);
  }
}
