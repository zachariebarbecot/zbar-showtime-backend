package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.PageNumber;
import fr.zbar.showtime.backend.core.search.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.search.domain.SearchPage;
import fr.zbar.showtime.backend.core.search.domain.SearchQuery;
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

  @Override
  public SearchPage searchAll(SearchQuery query, PageNumber page) {
    List<CatalogItem> matches =
        items.stream().filter(item -> item.title().equalsIgnoreCase(query.value())).toList();
    return new SearchPage(page.value(), 1, matches.size(), matches);
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
