package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.PageNumber;
import fr.zbar.showtime.backend.core.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import fr.zbar.showtime.backend.core.domain.SearchQuery;
import fr.zbar.showtime.backend.core.domain.SearchType;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class SearchTitlesService implements SearchTitles {

  private final SearchCatalog searchCatalog;

  @Override
  public SearchPage execute(SearchTitlesCommand command) {
    SearchQuery query = new SearchQuery(command.query());
    PageNumber page = new PageNumber(command.page());
    return switch (SearchType.from(command.type())) {
      case MOVIE -> searchCatalog.searchMovies(query, page);
      case ALL -> searchCatalog.searchAll(query, page);
      case TV -> searchCatalog.searchSeries(query, page);
    };
  }
}
