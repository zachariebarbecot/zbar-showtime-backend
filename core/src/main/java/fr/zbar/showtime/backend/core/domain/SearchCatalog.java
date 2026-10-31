package fr.zbar.showtime.backend.core.domain;

public interface SearchCatalog {

  SearchPage searchMovies(SearchQuery query, PageNumber page);
}
