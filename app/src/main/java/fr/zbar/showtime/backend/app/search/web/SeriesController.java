package fr.zbar.showtime.backend.app.search.web;

import fr.zbar.showtime.backend.core.search.application.GetSeriesDetail;
import fr.zbar.showtime.backend.core.search.application.GetSeriesDetailCommand;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/series", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
class SeriesController {

  private final GetSeriesDetail getSeriesDetail;

  @Operation(summary = "Get the detail of a series")
  @GetMapping("/{tmdbId}")
  SeriesDetailResponse detail(@PathVariable long tmdbId) {
    return SeriesDetailResponse.from(getSeriesDetail.execute(new GetSeriesDetailCommand(tmdbId)));
  }
}
