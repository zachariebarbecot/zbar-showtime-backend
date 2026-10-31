package fr.zbar.showtime.backend.core.domain;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;

public enum SearchType {
  MOVIE,
  TV,
  ALL;

  public static SearchType from(String value) {
    for (SearchType type : values()) {
      if (type.name().equals(value)) {
        return type;
      }
    }
    throw new InvalidFieldException("type", "Type must be MOVIE, TV or ALL");
  }
}
