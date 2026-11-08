package fr.zbar.showtime.backend.core.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import fr.zbar.showtime.backend.core.watchlist.application.InMemoryWatchedEpisodeRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant WATCHED_AT = Instant.parse("2026-03-01T20:30:00Z");

  private final GetSeasonDetailService service = serviceWith(SEASON_1, List.of());

  private static GetSeasonDetailService serviceWith(
      SeasonDetail season, List<WatchedEpisode> viewings) {
    return new GetSeasonDetailService(
        new InMemorySeasonCatalog(1396L, season), new InMemoryWatchedEpisodeRepository(viewings));
  }

  private static WatchedEpisode viewing(UUID userId, int seasonNumber, int episodeNumber) {
    return new WatchedEpisode(
        UUID.randomUUID(), userId, 1396L, seasonNumber, episodeNumber, WATCHED_AT);
  }

  @Test
  void should_return_the_season_when_the_series_and_the_season_are_known() {
    assertThat(service.execute(new GetSeasonDetailCommand(USER_ID, 1396L, 1)).season())
        .isEqualTo(SEASON_1);
  }

  @Test
  void should_return_the_specials_when_the_season_number_is_zero() {
    GetSeasonDetailService specials = serviceWith(SPECIALS, List.of());

    assertThat(specials.execute(new GetSeasonDetailCommand(USER_ID, 1396L, 0)).season())
        .isEqualTo(SPECIALS);
  }

  @Test
  void should_expose_the_viewing_date_of_a_watched_episode() {
    GetSeasonDetailService withViewing = serviceWith(SEASON_1, List.of(viewing(USER_ID, 1, 1)));

    SeasonDetailView view = withViewing.execute(new GetSeasonDetailCommand(USER_ID, 1396L, 1));

    assertThat(view.watchedAtByEpisode()).isEqualTo(Map.of(1, WATCHED_AT));
  }

  @Test
  void should_expose_no_viewing_when_the_user_watched_nothing_of_the_season() {
    assertThat(service.execute(new GetSeasonDetailCommand(USER_ID, 1396L, 1)).watchedAtByEpisode())
        .isEmpty();
  }

  @Test
  void should_ignore_the_viewings_of_other_users_and_other_seasons() {
    GetSeasonDetailService withViewings =
        serviceWith(SEASON_1, List.of(viewing(OTHER_USER_ID, 1, 1), viewing(USER_ID, 2, 1)));

    assertThat(
            withViewings
                .execute(new GetSeasonDetailCommand(USER_ID, 1396L, 1))
                .watchedAtByEpisode())
        .isEmpty();
  }

  @Test
  void should_expose_the_viewings_of_the_specials_season() {
    GetSeasonDetailService specials = serviceWith(SPECIALS, List.of(viewing(USER_ID, 0, 3)));

    assertThat(specials.execute(new GetSeasonDetailCommand(USER_ID, 1396L, 0)).watchedAtByEpisode())
        .isEqualTo(Map.of(3, WATCHED_AT));
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
            },
            new InMemoryWatchedEpisodeRepository(List.of()));

    assertThatThrownBy(
            () ->
                unreachableCatalog.execute(
                    new GetSeasonDetailCommand(USER_ID, tmdbId, seasonNumber)))
        .isInstanceOf(InvalidFieldException.class)
        .hasMessage(message);
  }

  @Test
  void should_report_the_tmdb_id_first_when_both_ids_are_invalid() {
    assertThatThrownBy(() -> service.execute(new GetSeasonDetailCommand(USER_ID, 0, -1)))
        .isInstanceOf(InvalidFieldException.class)
        .hasMessage("tmdbId: TMDB id must be greater than 0");
  }
}
