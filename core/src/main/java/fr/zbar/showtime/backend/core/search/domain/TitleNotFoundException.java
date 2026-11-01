package fr.zbar.showtime.backend.core.search.domain;

public class TitleNotFoundException extends RuntimeException {

  public TitleNotFoundException() {
    super("Title not found");
  }
}
