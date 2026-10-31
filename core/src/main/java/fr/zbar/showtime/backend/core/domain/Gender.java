package fr.zbar.showtime.backend.core.domain;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import org.apache.commons.lang3.StringUtils;

public enum Gender {
  MALE,
  FEMALE;

  public static Gender from(String value) {
    String stripped = StringUtils.strip(value);
    for (Gender gender : values()) {
      if (gender.name().equals(stripped)) {
        return gender;
      }
    }
    throw new InvalidFieldException("gender", "Gender must be MALE or FEMALE");
  }
}
