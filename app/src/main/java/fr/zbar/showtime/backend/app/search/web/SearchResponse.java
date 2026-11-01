package fr.zbar.showtime.backend.app.search.web;

import fr.zbar.showtime.backend.core.search.application.SearchResultItem;
import fr.zbar.showtime.backend.core.search.application.SearchResultPage;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import java.time.LocalDate;
import java.util.List;

public record SearchResponse(int page, int totalPages, long totalResults, List<Result> results) {

  static SearchResponse from(SearchResultPage searchPage) {
    return new SearchResponse(
        searchPage.page(),
        searchPage.totalPages(),
        searchPage.totalResults(),
        searchPage.results().stream().map(Result::from).toList());
  }

  public record Result(
      long id,
      MediaType type,
      String title,
      String posterUrl,
      LocalDate releaseDate,
      String watchlistStatus) {

    static Result from(SearchResultItem result) {
      CatalogItem item = result.item();
      return new Result(
          item.id(),
          item.type(),
          item.title(),
          item.posterUrl(),
          item.releaseDate(),
          result.watchlistStatus());
    }
  }
}
