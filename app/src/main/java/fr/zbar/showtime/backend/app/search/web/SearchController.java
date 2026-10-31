package fr.zbar.showtime.backend.app.search.web;

import fr.zbar.showtime.backend.core.search.application.SearchTitles;
import fr.zbar.showtime.backend.core.search.application.SearchTitlesCommand;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
class SearchController {

  private static final String DEFAULT_TYPE = "ALL";

  private final SearchTitles searchTitles;

  @Operation(summary = "Search movies, series or both by keyword")
  @GetMapping
  SearchResponse search(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String type) {
    return SearchResponse.from(
        searchTitles.execute(
            new SearchTitlesCommand(query, page, type == null ? DEFAULT_TYPE : type)));
  }
}
