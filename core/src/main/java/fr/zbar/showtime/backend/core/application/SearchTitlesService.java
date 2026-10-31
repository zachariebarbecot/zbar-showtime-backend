package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SearchTitlesService implements SearchTitles {

  private final SearchCatalog searchCatalog;

  @Override
  public SearchPage execute(SearchTitlesCommand command) {
    return null;
  }
}
