package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SearchPage;

public interface SearchTitles {

  SearchPage execute(SearchTitlesCommand command);
}
