package fr.zbar.showtime.backend.core.domain;

public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("Identifiants invalides");
  }
}
