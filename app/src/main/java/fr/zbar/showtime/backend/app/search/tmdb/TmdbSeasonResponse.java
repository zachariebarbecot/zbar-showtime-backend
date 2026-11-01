package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;

@JsonIgnoreProperties(ignoreUnknown = true)
record TmdbSeasonResponse() {

  SeasonDetail toSeasonDetail(TmdbProperties properties) {
    return null;
  }
}
