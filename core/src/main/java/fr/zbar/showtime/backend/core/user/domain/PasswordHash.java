package fr.zbar.showtime.backend.core.user.domain;

public record PasswordHash(String value) {

  @Override
  public String toString() {
    return "PasswordHash[****]";
  }
}
