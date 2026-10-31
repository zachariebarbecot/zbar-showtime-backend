package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.SearchPage;

public interface SearchTitles {

  SearchPage execute(SearchTitlesCommand command);
}
