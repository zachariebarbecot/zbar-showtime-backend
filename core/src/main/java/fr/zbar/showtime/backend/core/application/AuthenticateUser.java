package fr.zbar.showtime.backend.core.application;

public interface AuthenticateUser {

  String execute(AuthenticateUserCommand command);
}
