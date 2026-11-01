package fr.zbar.showtime.backend.core.search.domain;

public interface SeasonCatalog {

  SeasonDetail findSeason(long seriesId, int seasonNumber);
}
