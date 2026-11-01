package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.PageNumber;
import fr.zbar.showtime.backend.core.search.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.search.domain.SearchPage;
import fr.zbar.showtime.backend.core.search.domain.SearchQuery;
import fr.zbar.showtime.backend.core.search.domain.SearchType;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class SearchTitlesService implements SearchTitles {

  private final SearchCatalog searchCatalog;
  private final WatchlistMovieRepository watchlistMovieRepository;
  private final WatchlistSeriesRepository watchlistSeriesRepository;

  @Override
  public SearchResultPage execute(SearchTitlesCommand command) {
    SearchQuery query = new SearchQuery(command.query());
    PageNumber page = new PageNumber(command.page());
    SearchPage catalogPage =
        switch (SearchType.from(command.type())) {
          case MOVIE -> searchCatalog.searchMovies(query, page);
          case ALL -> searchCatalog.searchAll(query, page);
          case TV -> searchCatalog.searchSeries(query, page);
        };
    Map<Long, String> movieStatuses = movieStatuses(command.userId(), catalogPage.results());
    Map<Long, String> seriesStatuses = seriesStatuses(command.userId(), catalogPage.results());
    return new SearchResultPage(
        catalogPage.page(),
        catalogPage.totalPages(),
        catalogPage.totalResults(),
        catalogPage.results().stream()
            .map(
                item ->
                    new SearchResultItem(
                        item,
                        (item.type() == MediaType.MOVIE ? movieStatuses : seriesStatuses)
                            .get(item.id())))
            .toList());
  }

  private Map<Long, String> movieStatuses(UUID userId, List<CatalogItem> items) {
    List<Long> movieIds = idsOf(items, MediaType.MOVIE);
    if (movieIds.isEmpty()) {
      return Map.of();
    }
    return watchlistMovieRepository.findByUserIdAndTmdbIdIn(userId, movieIds).stream()
        .collect(Collectors.toMap(WatchlistMovie::tmdbId, movie -> movie.status().name()));
  }

  private Map<Long, String> seriesStatuses(UUID userId, List<CatalogItem> items) {
    List<Long> seriesIds = idsOf(items, MediaType.TV);
    if (seriesIds.isEmpty()) {
      return Map.of();
    }
    return watchlistSeriesRepository.findByUserIdAndTmdbIdIn(userId, seriesIds).stream()
        .collect(Collectors.toMap(WatchlistSeries::tmdbId, series -> series.status().name()));
  }

  private static List<Long> idsOf(List<CatalogItem> items, MediaType type) {
    return items.stream().filter(item -> item.type() == type).map(CatalogItem::id).toList();
  }
}
