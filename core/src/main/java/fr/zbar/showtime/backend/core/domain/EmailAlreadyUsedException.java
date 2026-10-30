package fr.zbar.showtime.backend.core.domain;

public class EmailAlreadyUsedException extends RuntimeException {

  public EmailAlreadyUsedException() {
    super("Email already used");
  }
}
