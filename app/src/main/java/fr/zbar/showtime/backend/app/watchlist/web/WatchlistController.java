package fr.zbar.showtime.backend.app.watchlist.web;

import fr.zbar.showtime.backend.core.watchlist.application.AddMovieToWatchlist;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/watchlist", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
class WatchlistController {

  private final AddMovieToWatchlist addMovieToWatchlist;

  @Operation(summary = "Add a movie to the watchlist of the token owner")
  @PostMapping(path = "/movies", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  WatchlistMovieResponse addMovie(
      @AuthenticationPrincipal Jwt jwt, @RequestBody AddMovieRequest request) {
    return WatchlistMovieResponse.from(
        addMovieToWatchlist.execute(request.toCommand(UUID.fromString(jwt.getSubject()))));
  }
}
