package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.util.List;

public class InMemoryTitleCatalog implements TitleCatalog {

  private final List<CatalogItem> movies;

  public InMemoryTitleCatalog(CatalogItem... movies) {
    this.movies = List.of(movies);
  }

  @Override
  public CatalogItem findMovie(long id) {
    return movies.stream()
        .filter(movie -> movie.id() == id)
        .findFirst()
        .orElseThrow(TitleNotFoundException::new);
  }
}
