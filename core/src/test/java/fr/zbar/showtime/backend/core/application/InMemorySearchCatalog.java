package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.CatalogItem;
import fr.zbar.showtime.backend.core.domain.MediaType;
import fr.zbar.showtime.backend.core.domain.PageNumber;
import fr.zbar.showtime.backend.core.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import fr.zbar.showtime.backend.core.domain.SearchQuery;
import java.util.List;

class InMemorySearchCatalog implements SearchCatalog {

  private final List<CatalogItem> items;

  InMemorySearchCatalog(CatalogItem... items) {
    this.items = List.of(items);
  }

  @Override
  public SearchPage searchMovies(SearchQuery query, PageNumber page) {
    return search(MediaType.MOVIE, query, page);
  }

  @Override
  public SearchPage searchSeries(SearchQuery query, PageNumber page) {
    return search(MediaType.TV, query, page);
  }

  private SearchPage search(MediaType type, SearchQuery query, PageNumber page) {
    List<CatalogItem> matches =
        items.stream()
            .filter(item -> item.type() == type)
            .filter(item -> item.title().equalsIgnoreCase(query.value()))
            .toList();
    return new SearchPage(page.value(), 1, matches.size(), matches);
  }
}
