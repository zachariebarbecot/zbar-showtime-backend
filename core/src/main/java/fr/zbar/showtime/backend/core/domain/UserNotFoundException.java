package fr.zbar.showtime.backend.core.domain;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException() {
    super("User not found");
  }
}
