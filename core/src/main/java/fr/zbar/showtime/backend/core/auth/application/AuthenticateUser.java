package fr.zbar.showtime.backend.core.auth.application;

public interface AuthenticateUser {

  String execute(AuthenticateUserCommand command);
}
