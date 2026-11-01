package fr.zbar.showtime.backend.app.watchlist.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
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
class RemoveMovieFromWatchlistIT extends AbstractIT {

  @MockitoBean private TitleCatalog titleCatalog;

  @Autowired private JdbcTemplate jdbcTemplate;

  private String newUser() {
    String email = "watchlist-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    return email;
  }

  private void addMovie(String email, long tmdbId) {
    when(titleCatalog.findMovie(tmdbId))
        .thenReturn(new CatalogItem(tmdbId, MediaType.MOVIE, "Fight Club", null, null));
    restTestClient
        .post()
        .uri("/watchlist/movies")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", tmdbId))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  private RestTestClient.ResponseSpec remove(String email, long tmdbId) {
    return restTestClient
        .delete()
        .uri("/watchlist/movies/{tmdbId}", tmdbId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange();
  }

  private int movieCount(String email, long tmdbId) {
    return jdbcTemplate.queryForObject(
        "select count(*) from watchlist_movie m"
            + " join users u on u.id = m.user_id where u.email = ? and m.tmdb_id = ?",
        Integer.class,
        email,
        tmdbId);
  }

  @Test
  void should_return_204_without_body_and_remove_the_movie_when_it_is_in_my_watchlist() {
    String email = newUser();
    addMovie(email, 550L);

    remove(email, 550L).expectStatus().isNoContent().expectBody().isEmpty();

    assertThat(movieCount(email, 550L)).isZero();
  }

  @Test
  void should_return_404_when_the_movie_is_not_in_my_watchlist() {
    String email = newUser();

    remove(email, 550L)
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Watchlist entry not found");
  }

  @Test
  void should_return_404_and_keep_the_movie_of_another_user_when_only_they_have_it() {
    String owner = newUser();
    String other = newUser();
    addMovie(owner, 550L);

    remove(other, 550L).expectStatus().isNotFound();

    assertThat(movieCount(owner, 550L)).isEqualTo(1);
  }

  @Test
  void should_keep_the_movie_of_the_other_user_when_both_have_it() {
    String first = newUser();
    String second = newUser();
    addMovie(first, 550L);
    addMovie(second, 550L);

    remove(first, 550L).expectStatus().isNoContent();

    assertThat(movieCount(first, 550L)).isZero();
    assertThat(movieCount(second, 550L)).isEqualTo(1);
  }
}
