package fr.zbar.showtime.backend.core.search.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

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
}
