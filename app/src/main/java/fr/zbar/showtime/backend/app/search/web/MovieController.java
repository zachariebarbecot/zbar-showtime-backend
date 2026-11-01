package fr.zbar.showtime.backend.app.search.web;

import fr.zbar.showtime.backend.core.search.application.GetMovieDetail;
import fr.zbar.showtime.backend.core.search.application.GetMovieDetailCommand;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/movies", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
class MovieController {

  private final GetMovieDetail getMovieDetail;

  @Operation(summary = "Get the detail of a movie")
  @GetMapping("/{tmdbId}")
  MovieDetailResponse detail(@AuthenticationPrincipal Jwt jwt, @PathVariable long tmdbId) {
    return MovieDetailResponse.from(
        getMovieDetail.execute(
            new GetMovieDetailCommand(UUID.fromString(jwt.getSubject()), tmdbId)));
  }
}
