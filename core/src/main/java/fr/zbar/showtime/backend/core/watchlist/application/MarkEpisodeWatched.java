package fr.zbar.showtime.backend.core.watchlist.application;

public interface MarkEpisodeWatched {

  void execute(MarkEpisodeWatchedCommand command);
}
