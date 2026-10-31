package fr.zbar.showtime.backend.core.user.domain;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException() {
    super("User not found");
  }
}
