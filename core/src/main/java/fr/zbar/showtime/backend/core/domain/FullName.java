package fr.zbar.showtime.backend.core.domain;

public record FullName(String lastName, String firstName) {

  public FullName {
    if (lastName == null || lastName.isBlank()) {
      throw new InvalidFieldException("lastName", "Last name cannot be blank");
    }
    if (firstName == null || firstName.isBlank()) {
      throw new InvalidFieldException("firstName", "First name cannot be blank");
    }
  }
}
