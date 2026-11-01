package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.util.List;

public class InMemoryTitleCatalog implements TitleCatalog {

  private final List<CatalogItem> items;

  public InMemoryTitleCatalog(CatalogItem... items) {
    this.items = List.of(items);
  }

  @Override
  public CatalogItem findMovie(long id) {
    return find(id, MediaType.MOVIE);
  }

  @Override
  public CatalogItem findSeries(long id) {
    return find(id, MediaType.TV);
  }

  private CatalogItem find(long id, MediaType type) {
    return items.stream()
        .filter(item -> item.id() == id && item.type() == type)
        .findFirst()
        .orElseThrow(TitleNotFoundException::new);
  }
}
