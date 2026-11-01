package fr.zbar.showtime.backend.app.watchlist.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
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
class AddMovieToWatchlistIT extends AbstractIT {

  private static final CatalogItem MATRIX =
      new CatalogItem(603L, MediaType.MOVIE, "Matrix", null, null);

  @MockitoBean private TitleCatalog titleCatalog;

  @Autowired private JdbcTemplate jdbcTemplate;

  private String newUserEmail() {
    String email = "watchlist-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    return email;
  }

  private RestTestClient.ResponseSpec addMovie(String email, long tmdbId) {
    return restTestClient
        .post()
        .uri("/watchlist/movies")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", tmdbId))
        .exchange();
  }

  @Test
  void should_return_201_with_a_to_watch_entry_when_the_movie_is_known() {
    String email = newUserEmail();
    when(titleCatalog.findMovie(603L)).thenReturn(MATRIX);

    addMovie(email, 603L)
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.tmdbId")
        .isEqualTo(603)
        .jsonPath("$.status")
        .isEqualTo("TO_WATCH")
        .jsonPath("$.addedAt")
        .isEqualTo("2025-06-15T10:00:00Z")
        .jsonPath("$.watchedAt")
        .doesNotExist();
    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select m.status, m.added_at, m.watched_at from watchlist_movie m"
                + " join users u on u.id = m.user_id where u.email = ? and m.tmdb_id = 603",
            email);
    assertThat(row).containsEntry("status", "TO_WATCH").containsEntry("watched_at", null);
  }

  private List<UUID> entryIds(String email) {
    return jdbcTemplate.queryForList(
        "select m.id from watchlist_movie m join users u on u.id = m.user_id"
            + " where u.email = ? and m.tmdb_id = 603",
        UUID.class,
        email);
  }

  @Test
  void should_return_409_and_keep_the_existing_entry_when_the_movie_is_already_in_the_watchlist() {
    String email = newUserEmail();
    when(titleCatalog.findMovie(603L)).thenReturn(MATRIX);
    addMovie(email, 603L).expectStatus().isCreated();
    List<UUID> existing = entryIds(email);

    addMovie(email, 603L)
        .expectStatus()
        .isEqualTo(409)
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Title already in watchlist");

    assertThat(entryIds(email)).isEqualTo(existing).hasSize(1);
  }

  @Test
  void should_return_201_for_each_user_when_two_users_add_the_same_movie() {
    String first = newUserEmail();
    String second = newUserEmail();
    when(titleCatalog.findMovie(603L)).thenReturn(MATRIX);
    addMovie(first, 603L).expectStatus().isCreated();

    addMovie(second, 603L).expectStatus().isCreated();

    assertThat(entryIds(first)).hasSize(1);
    assertThat(entryIds(second)).hasSize(1);
  }

  @Test
  void should_return_404_and_create_no_entry_when_tmdb_does_not_know_the_movie() {
    String email = newUserEmail();
    when(titleCatalog.findMovie(603L)).thenThrow(new TitleNotFoundException());

    addMovie(email, 603L)
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
    when(titleCatalog.findMovie(603L))
        .thenThrow(new CatalogUnavailableException(new IllegalStateException("down")));

    addMovie(email, 603L).expectStatus().isEqualTo(502);

    assertThat(entryIds(email)).isEmpty();
  }

  @Test
  void should_return_401_and_create_no_entry_when_the_request_is_not_authenticated() {
    String email = newUserEmail();

    restTestClient
        .post()
        .uri("/watchlist/movies")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", 603L))
        .exchange()
        .expectStatus()
        .isUnauthorized();

    assertThat(entryIds(email)).isEmpty();
  }
}
