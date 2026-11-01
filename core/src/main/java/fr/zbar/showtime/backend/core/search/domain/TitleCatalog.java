package fr.zbar.showtime.backend.core.search.domain;

public interface TitleCatalog {

  CatalogItem findMovie(long id);

  CatalogItem findSeries(long id);
}
