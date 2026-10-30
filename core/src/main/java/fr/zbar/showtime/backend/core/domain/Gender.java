package fr.zbar.showtime.backend.core.domain;

public enum Gender {
  MALE,
  FEMALE;

  public static Gender from(String value) {
    for (Gender gender : values()) {
      if (gender.name().equals(value)) {
        return gender;
      }
    }
    throw new InvalidFieldException("gender", "Gender must be MALE or FEMALE");
  }
}
