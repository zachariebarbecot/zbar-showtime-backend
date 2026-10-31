package fr.zbar.showtime.backend.app.search.web;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.SearchPage;
import java.time.LocalDate;
import java.util.List;

public record SearchResponse(int page, int totalPages, long totalResults, List<Result> results) {

  static SearchResponse from(SearchPage searchPage) {
    return new SearchResponse(
        searchPage.page(),
        searchPage.totalPages(),
        searchPage.totalResults(),
        searchPage.results().stream().map(Result::from).toList());
  }

  public record Result(
      long id, MediaType type, String title, String posterUrl, LocalDate releaseDate) {

    static Result from(CatalogItem item) {
      return new Result(item.id(), item.type(), item.title(), item.posterUrl(), item.releaseDate());
    }
  }
}
