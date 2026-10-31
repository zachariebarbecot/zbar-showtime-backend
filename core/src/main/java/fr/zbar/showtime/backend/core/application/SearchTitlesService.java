package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.PageNumber;
import fr.zbar.showtime.backend.core.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import fr.zbar.showtime.backend.core.domain.SearchQuery;
import fr.zbar.showtime.backend.core.domain.SearchType;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class SearchTitlesService implements SearchTitles {

  private final SearchCatalog searchCatalog;

  @Override
  public SearchPage execute(SearchTitlesCommand command) {
    SearchQuery query = new SearchQuery(command.query());
    PageNumber page = new PageNumber(command.page());
    return SearchType.from(command.type()) == SearchType.TV
        ? searchCatalog.searchSeries(query, page)
        : searchCatalog.searchMovies(query, page);
  }
}
