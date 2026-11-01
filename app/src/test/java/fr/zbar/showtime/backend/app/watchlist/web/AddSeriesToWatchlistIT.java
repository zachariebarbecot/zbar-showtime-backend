package fr.zbar.showtime.backend.app.watchlist.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.app.support.FixedClockConfiguration;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class AddSeriesToWatchlistIT extends AbstractIT {

  private static final CatalogItem GOT = new CatalogItem(1399L, MediaType.TV, "GoT", null, null);

  @MockitoBean private TitleCatalog titleCatalog;

  @Autowired private JdbcTemplate jdbcTemplate;

  private String newUserEmail() {
    String email = "watchlist-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    return email;
  }

  private RestTestClient.ResponseSpec addSeries(String email, long tmdbId) {
    return restTestClient
        .post()
        .uri("/watchlist/series")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", tmdbId))
        .exchange();
  }

  @Test
  void should_return_201_with_a_to_watch_entry_when_the_series_is_known() {
    String email = newUserEmail();
    when(titleCatalog.findSeries(1399L)).thenReturn(GOT);

    addSeries(email, 1399L)
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.tmdbId")
        .isEqualTo(1399)
        .jsonPath("$.status")
        .isEqualTo("TO_WATCH")
        .jsonPath("$.addedAt")
        .isEqualTo(FixedClockConfiguration.NOW.toString())
        .jsonPath("$.watchedAt")
        .doesNotExist();
    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select s.status, s.watched_at from watchlist_series s"
                + " join users u on u.id = s.user_id where u.email = ? and s.tmdb_id = 1399",
            email);
    assertThat(row).containsEntry("status", "TO_WATCH").containsEntry("watched_at", null);
  }

  private List<UUID> entryIds(String email) {
    return jdbcTemplate.queryForList(
        "select s.id from watchlist_series s join users u on u.id = s.user_id"
            + " where u.email = ? and s.tmdb_id = 1399",
        UUID.class,
        email);
  }

  @Test
  void should_return_409_and_keep_the_existing_entry_when_the_series_is_already_in_the_watchlist() {
    String email = newUserEmail();
    when(titleCatalog.findSeries(1399L)).thenReturn(GOT);
    addSeries(email, 1399L).expectStatus().isCreated();
    List<UUID> existing = entryIds(email);

    addSeries(email, 1399L)
        .expectStatus()
        .isEqualTo(409)
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Title already in watchlist");

    assertThat(entryIds(email)).isEqualTo(existing).hasSize(1);
  }

  @Test
  void should_return_201_for_each_user_when_two_users_add_the_same_series() {
    String first = newUserEmail();
    String second = newUserEmail();
    when(titleCatalog.findSeries(1399L)).thenReturn(GOT);
    addSeries(first, 1399L).expectStatus().isCreated();

    addSeries(second, 1399L).expectStatus().isCreated();

    assertThat(entryIds(first)).hasSize(1);
    assertThat(entryIds(second)).hasSize(1);
  }

  @Test
  void should_return_404_and_create_no_entry_when_tmdb_does_not_know_the_series() {
    String email = newUserEmail();
    when(titleCatalog.findSeries(1399L)).thenThrow(new TitleNotFoundException());

    addSeries(email, 1399L)
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Title not found");

    assertThat(entryIds(email)).isEmpty();
  }

  @Test
  void should_return_502_and_create_no_entry_when_tmdb_is_unavailable() {
    String email = newUserEmail();
    when(titleCatalog.findSeries(1399L))
        .thenThrow(new CatalogUnavailableException(new IllegalStateException("down")));

    addSeries(email, 1399L).expectStatus().isEqualTo(502);

    assertThat(entryIds(email)).isEmpty();
  }

  @Test
  void should_return_201_when_a_movie_with_the_same_id_is_in_the_movie_watchlist() {
    String email = newUserEmail();
    when(titleCatalog.findMovie(1399L))
        .thenReturn(new CatalogItem(1399L, MediaType.MOVIE, "Movie", null, null));
    when(titleCatalog.findSeries(1399L)).thenReturn(GOT);
    restTestClient
        .post()
        .uri("/watchlist/movies")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", 1399L))
        .exchange()
        .expectStatus()
        .isCreated();

    addSeries(email, 1399L).expectStatus().isCreated();

    assertThat(entryIds(email)).hasSize(1);
  }

  @Test
  void should_return_401_and_create_no_entry_when_the_request_is_not_authenticated() {
    String email = newUserEmail();

    restTestClient
        .post()
        .uri("/watchlist/series")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", 1399L))
        .exchange()
        .expectStatus()
        .isUnauthorized();

    assertThat(entryIds(email)).isEmpty();
  }
}
