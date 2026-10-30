package fr.zbar.showtime.backend.core.domain;

import java.util.List;

public record RawPassword(String value) {

  public List<String> violations(Email email) {
    return List.of();
  }
}
