package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.AuthenticateUserCommand;

public record LoginRequest(String email, String password) {

  AuthenticateUserCommand toCommand() {
    return new AuthenticateUserCommand(email, password);
  }
}
