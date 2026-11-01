package fr.zbar.showtime.backend.app.search.web;

import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class SeasonDetailIT extends AbstractIT {

  @MockitoBean private SeasonCatalog seasonCatalog;

  private RestTestClient.ResponseSpec season(String email, String id, String seasonNumber) {
    return restTestClient
        .get()
        .uri("/series/{id}/seasons/{n}", id, seasonNumber)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange();
  }

  @Test
  void should_return_200_with_the_episodes_when_tmdb_knows_the_season() {
    registerUser("season-ca1@mail.fr");
    when(seasonCatalog.findSeason(1396L, 1))
        .thenReturn(
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
                        "https://image.tmdb.org/t/p/w300/e1.jpg"),
                    new Episode(2, "Le chat", null, null, null, null))));

    season("season-ca1@mail.fr", "1396", "1")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.seasonNumber")
        .isEqualTo(1)
        .jsonPath("$.name")
        .isEqualTo("Saison 1")
        .jsonPath("$.overview")
        .isEqualTo("Les débuts.")
        .jsonPath("$.airDate")
        .isEqualTo("2008-01-20")
        .jsonPath("$.posterUrl")
        .isEqualTo("https://image.tmdb.org/t/p/w342/s1.jpg")
        .jsonPath("$.episodes[0].episodeNumber")
        .isEqualTo(1)
        .jsonPath("$.episodes[0].name")
        .isEqualTo("Pilote")
        .jsonPath("$.episodes[0].overview")
        .isEqualTo("Walter apprend sa maladie.")
        .jsonPath("$.episodes[0].runtime")
        .isEqualTo(58)
        .jsonPath("$.episodes[0].airDate")
        .isEqualTo("2008-01-20")
        .jsonPath("$.episodes[0].stillUrl")
        .isEqualTo("https://image.tmdb.org/t/p/w300/e1.jpg")
        .jsonPath("$.episodes[1].episodeNumber")
        .isEqualTo(2);
  }

  @Test
  void should_return_401_when_there_is_no_token() {
    restTestClient.get().uri("/series/1396/seasons/1").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void should_return_200_with_the_specials_when_the_season_number_is_zero() {
    registerUser("season-ca3@mail.fr");
    when(seasonCatalog.findSeason(1396L, 0))
        .thenReturn(
            new SeasonDetail(
                0,
                "Épisodes spéciaux",
                null,
                null,
                null,
                List.of(new Episode(1, "À venir", null, null, LocalDate.of(2999, 12, 31), null))));

    season("season-ca3@mail.fr", "1396", "0")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo("Épisodes spéciaux")
        .jsonPath("$.episodes[0].airDate")
        .isEqualTo("2999-12-31");
  }
}
