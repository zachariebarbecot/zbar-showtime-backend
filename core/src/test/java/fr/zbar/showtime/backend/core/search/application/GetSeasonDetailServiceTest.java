package fr.zbar.showtime.backend.core.search.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

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

  private final GetSeasonDetailService service =
      new GetSeasonDetailService(new InMemorySeasonCatalog(1396L, SEASON_1));

  @Test
  void should_return_the_season_when_the_series_and_the_season_are_known() {
    assertThat(service.execute(new GetSeasonDetailCommand(1396L, 1))).isEqualTo(SEASON_1);
  }
}
