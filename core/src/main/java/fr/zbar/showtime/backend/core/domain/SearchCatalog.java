package fr.zbar.showtime.backend.core.domain;

public interface SearchCatalog {

  SearchPage searchMovies(SearchQuery query, PageNumber page);

  SearchPage searchSeries(SearchQuery query, PageNumber page);

  SearchPage searchAll(SearchQuery query, PageNumber page);
}
