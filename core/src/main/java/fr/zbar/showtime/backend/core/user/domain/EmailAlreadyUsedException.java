package fr.zbar.showtime.backend.core.user.domain;

public class EmailAlreadyUsedException extends RuntimeException {

  public EmailAlreadyUsedException() {
    super("Email already used");
  }
}
