package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.SearchTitles;
import fr.zbar.showtime.backend.core.application.SearchTitlesCommand;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
class SearchController {

  private static final String DEFAULT_TYPE = "MOVIE";

  private final SearchTitles searchTitles;

  @Operation(summary = "Search movies by keyword")
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
