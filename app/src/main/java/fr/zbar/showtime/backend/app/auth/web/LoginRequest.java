package fr.zbar.showtime.backend.app.auth.web;

import fr.zbar.showtime.backend.core.auth.application.AuthenticateUserCommand;

public record LoginRequest(String email, String password) {

  AuthenticateUserCommand toCommand() {
    return new AuthenticateUserCommand(email, password);
  }
}
