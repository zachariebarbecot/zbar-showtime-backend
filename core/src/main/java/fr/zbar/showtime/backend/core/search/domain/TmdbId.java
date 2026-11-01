package fr.zbar.showtime.backend.core.search.domain;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;

public record TmdbId(long value) {

  public TmdbId {
    if (value <= 0) {
      throw new InvalidFieldException("tmdbId", "TMDB id must be greater than 0");
    }
  }
}
