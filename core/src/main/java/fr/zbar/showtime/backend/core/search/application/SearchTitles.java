package fr.zbar.showtime.backend.core.search.application;

public interface SearchTitles {

  SearchResultPage execute(SearchTitlesCommand command);
}
