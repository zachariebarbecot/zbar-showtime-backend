package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieDetailCatalog;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetMovieDetailService implements GetMovieDetail {

  private final MovieDetailCatalog movieDetailCatalog;

  @Override
  public MovieDetail execute(long tmdbId) {
    return null;
  }
}
