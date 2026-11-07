package fr.zbar.showtime.backend.app.watchlist.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class UnmarkEpisodeWatchedIT extends AbstractIT {

  private static final long SERIES_ID = 1396L;
  private static final String EPISODE_PATH =
      "/watchlist/series/{tmdbId}/seasons/{season}/episodes/{episode}/watched";

  @MockitoBean private TitleCatalog titleCatalog;

  @MockitoBean private SeasonCatalog seasonCatalog;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void stubCatalogs() {
    when(titleCatalog.findSeries(SERIES_ID))
        .thenReturn(new CatalogItem(SERIES_ID, MediaType.TV, "Breaking Bad", null, null));
    when(seasonCatalog.findSeason(SERIES_ID, 1))
        .thenReturn(
            new SeasonDetail(
                1,
                "Saison 1",
                null,
                null,
                null,
                List.of(
                    new Episode(1, "Pilote", null, null, null, null),
                    new Episode(2, "Le chat", null, null, null, null))));
  }

  private String newUserWithSeries() {
    String email = "unmark-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    restTestClient
        .post()
        .uri("/watchlist/series")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", SERIES_ID))
        .exchange()
        .expectStatus()
        .isCreated();
    return email;
  }

  private void mark(String email, int episode) {
    restTestClient
        .post()
        .uri(EPISODE_PATH, SERIES_ID, 1, episode)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  private RestTestClient.ResponseSpec unmark(String email, int episode) {
    return restTestClient
        .delete()
        .uri(EPISODE_PATH, SERIES_ID, 1, episode)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange();
  }

  private List<Integer> viewedEpisodes(String email) {
    return jdbcTemplate.queryForList(
        "select w.episode_number from watched_episode w"
            + " join users u on u.id = w.user_id where u.email = ? and w.series_tmdb_id = ?",
        Integer.class,
        email,
        SERIES_ID);
  }

  @Test
  void should_return_204_without_body_and_delete_only_that_viewing_when_the_episode_is_watched() {
    String email = newUserWithSeries();
    mark(email, 1);
    mark(email, 2);

    unmark(email, 2).expectStatus().isNoContent().expectBody().isEmpty();

    assertThat(viewedEpisodes(email)).containsExactly(1);
  }
}
