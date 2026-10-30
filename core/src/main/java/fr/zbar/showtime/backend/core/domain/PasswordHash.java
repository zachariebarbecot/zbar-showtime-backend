package fr.zbar.showtime.backend.core.domain;

public record PasswordHash(String value) {

  @Override
  public String toString() {
    return "PasswordHash[****]";
  }
}
