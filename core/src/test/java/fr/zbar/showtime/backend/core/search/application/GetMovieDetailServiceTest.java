package fr.zbar.showtime.backend.core.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GetMovieDetailServiceTest {

  private static final MovieDetail MATRIX =
      new MovieDetail(
          603L,
          "Matrix",
          "The Matrix",
          List.of("Action"),
          LocalDate.of(1999, 3, 31),
          136,
          MovieStatus.RELEASED,
          "Un hacker découvre la réalité.",
          "https://image.tmdb.org/t/p/w342/m.jpg",
          List.of(),
          List.of(),
          List.of());

  private final GetMovieDetailService service =
      new GetMovieDetailService(new InMemoryMovieDetailCatalog(MATRIX));

  @Test
  void should_return_the_movie_detail_when_the_id_is_known() {
    assertThat(service.execute(603L)).isEqualTo(MATRIX);
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, -1L})
  void should_throw_invalid_field_without_calling_the_catalog_when_the_id_is_not_positive(
      long tmdbId) {
    GetMovieDetailService unreachableCatalog =
        new GetMovieDetailService(
            id -> {
              throw new AssertionError("catalog must not be called");
            });

    assertThatThrownBy(() -> unreachableCatalog.execute(tmdbId))
        .isInstanceOf(InvalidFieldException.class)
        .hasMessage("tmdbId: TMDB id must be greater than 0");
  }
}
