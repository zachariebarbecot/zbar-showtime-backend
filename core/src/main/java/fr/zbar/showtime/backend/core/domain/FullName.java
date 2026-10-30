package fr.zbar.showtime.backend.core.domain;

import org.apache.commons.lang3.StringUtils;

public record FullName(String lastName, String firstName) {

  public FullName {
    if (StringUtils.isBlank(lastName)) {
      throw new InvalidFieldException("lastName", "Last name cannot be blank");
    }
    if (StringUtils.isBlank(firstName)) {
      throw new InvalidFieldException("firstName", "First name cannot be blank");
    }
  }
}
