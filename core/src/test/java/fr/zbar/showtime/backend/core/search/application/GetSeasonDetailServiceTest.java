package fr.zbar.showtime.backend.core.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GetSeasonDetailServiceTest {

  private static final SeasonDetail SEASON_1 =
      new SeasonDetail(
          1,
          "Saison 1",
          "Les débuts.",
          LocalDate.of(2008, 1, 20),
          "https://image.tmdb.org/t/p/w342/s1.jpg",
          List.of(
              new Episode(
                  1,
                  "Pilote",
                  "Walter apprend sa maladie.",
                  58,
                  LocalDate.of(2008, 1, 20),
                  "https://image.tmdb.org/t/p/w300/e1.jpg")));

  private static final SeasonDetail SPECIALS =
      new SeasonDetail(0, "Épisodes spéciaux", null, null, null, List.of());

  private final GetSeasonDetailService service =
      new GetSeasonDetailService(new InMemorySeasonCatalog(1396L, SEASON_1));

  @Test
  void should_return_the_season_when_the_series_and_the_season_are_known() {
    assertThat(service.execute(new GetSeasonDetailCommand(1396L, 1))).isEqualTo(SEASON_1);
  }

  @Test
  void should_return_the_specials_when_the_season_number_is_zero() {
    GetSeasonDetailService specials =
        new GetSeasonDetailService(new InMemorySeasonCatalog(1396L, SPECIALS));

    assertThat(specials.execute(new GetSeasonDetailCommand(1396L, 0))).isEqualTo(SPECIALS);
  }

  @ParameterizedTest
  @CsvSource({
    "0,1,tmdbId: TMDB id must be greater than 0",
    "-1,1,tmdbId: TMDB id must be greater than 0",
    "1396,-1,seasonNumber: Season number must not be negative"
  })
  void should_throw_invalid_field_without_calling_the_catalog_when_the_ids_are_invalid(
      long tmdbId, int seasonNumber, String message) {
    GetSeasonDetailService unreachableCatalog =
        new GetSeasonDetailService(
            (seriesId, number) -> {
              throw new AssertionError("catalog must not be called");
            });

    assertThatThrownBy(
            () -> unreachableCatalog.execute(new GetSeasonDetailCommand(tmdbId, seasonNumber)))
        .isInstanceOf(InvalidFieldException.class)
        .hasMessage(message);
  }

  @Test
  void should_report_the_tmdb_id_first_when_both_ids_are_invalid() {
    assertThatThrownBy(() -> service.execute(new GetSeasonDetailCommand(0, -1)))
        .isInstanceOf(InvalidFieldException.class)
        .hasMessage("tmdbId: TMDB id must be greater than 0");
  }
}
