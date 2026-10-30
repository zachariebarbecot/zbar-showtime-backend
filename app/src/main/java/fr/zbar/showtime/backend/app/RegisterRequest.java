package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.RegisterUserCommand;
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
