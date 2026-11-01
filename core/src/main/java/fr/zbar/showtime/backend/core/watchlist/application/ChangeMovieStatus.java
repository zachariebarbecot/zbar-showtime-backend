package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;

public interface ChangeMovieStatus {

  WatchlistMovie execute(ChangeMovieStatusCommand command);
}
