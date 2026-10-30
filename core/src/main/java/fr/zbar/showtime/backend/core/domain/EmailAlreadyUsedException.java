package fr.zbar.showtime.backend.core.domain;

public class EmailAlreadyUsedException extends RuntimeException {

  public EmailAlreadyUsedException(Email email) {
    super("Email already used");
  }
}
