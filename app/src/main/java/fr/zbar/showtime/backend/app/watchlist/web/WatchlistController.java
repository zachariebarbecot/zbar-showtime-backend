package fr.zbar.showtime.backend.app.watchlist.web;

import fr.zbar.showtime.backend.core.watchlist.application.AddMovieToWatchlist;
import fr.zbar.showtime.backend.core.watchlist.application.AddSeriesToWatchlist;
import fr.zbar.showtime.backend.core.watchlist.application.ChangeMovieStatus;
import fr.zbar.showtime.backend.core.watchlist.application.ChangeSeriesStatus;
import fr.zbar.showtime.backend.core.watchlist.application.RemoveMovieFromWatchlist;
import fr.zbar.showtime.backend.core.watchlist.application.RemoveMovieFromWatchlistCommand;
import fr.zbar.showtime.backend.core.watchlist.application.RemoveSeriesFromWatchlist;
import fr.zbar.showtime.backend.core.watchlist.application.RemoveSeriesFromWatchlistCommand;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
  private final ChangeMovieStatus changeMovieStatus;
  private final RemoveMovieFromWatchlist removeMovieFromWatchlist;
  private final AddSeriesToWatchlist addSeriesToWatchlist;
  private final ChangeSeriesStatus changeSeriesStatus;
  private final RemoveSeriesFromWatchlist removeSeriesFromWatchlist;

  @Operation(summary = "Add a movie to the watchlist of the token owner")
  @PostMapping(path = "/movies", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  void addMovie(@AuthenticationPrincipal Jwt jwt, @RequestBody AddMovieRequest request) {
    addMovieToWatchlist.execute(request.toCommand(UUID.fromString(jwt.getSubject())));
  }

  @Operation(summary = "Change the status of a movie of the watchlist of the token owner")
  @PatchMapping(path = "/movies/{tmdbId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  WatchlistMovieResponse changeMovieStatus(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable long tmdbId,
      @RequestBody ChangeMovieStatusRequest request) {
    return WatchlistMovieResponse.from(
        changeMovieStatus.execute(request.toCommand(UUID.fromString(jwt.getSubject()), tmdbId)));
  }

  @Operation(summary = "Remove a movie from the watchlist of the token owner")
  @DeleteMapping("/movies/{tmdbId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void removeMovie(@AuthenticationPrincipal Jwt jwt, @PathVariable long tmdbId) {
    removeMovieFromWatchlist.execute(
        new RemoveMovieFromWatchlistCommand(UUID.fromString(jwt.getSubject()), tmdbId));
  }

  @Operation(summary = "Add a series to the watchlist of the token owner")
  @PostMapping(path = "/series", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  void addSeries(@AuthenticationPrincipal Jwt jwt, @RequestBody AddSeriesRequest request) {
    addSeriesToWatchlist.execute(request.toCommand(UUID.fromString(jwt.getSubject())));
  }

  @Operation(summary = "Change the status of a series of the watchlist of the token owner")
  @PatchMapping(path = "/series/{tmdbId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  WatchlistSeriesResponse changeSeriesStatus(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable long tmdbId,
      @RequestBody ChangeSeriesStatusRequest request) {
    return WatchlistSeriesResponse.from(
        changeSeriesStatus.execute(request.toCommand(UUID.fromString(jwt.getSubject()), tmdbId)));
  }

  @Operation(summary = "Remove a series from the watchlist of the token owner")
  @DeleteMapping("/series/{tmdbId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void removeSeries(@AuthenticationPrincipal Jwt jwt, @PathVariable long tmdbId) {
    removeSeriesFromWatchlist.execute(
        new RemoveSeriesFromWatchlistCommand(UUID.fromString(jwt.getSubject()), tmdbId));
  }
}
