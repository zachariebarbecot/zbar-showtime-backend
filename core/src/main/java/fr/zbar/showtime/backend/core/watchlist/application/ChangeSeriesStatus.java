package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;

public interface ChangeSeriesStatus {

  WatchlistSeries execute(ChangeSeriesStatusCommand command);
}
