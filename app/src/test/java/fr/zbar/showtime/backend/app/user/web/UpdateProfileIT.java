package fr.zbar.showtime.backend.app.user.web;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.app.support.FixedClockConfiguration;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class UpdateProfileIT extends AbstractIT {

  private static final Instant REGISTERED_AT = Instant.parse("2025-01-01T08:00:00Z");

  @Autowired private JdbcTemplate jdbcTemplate;

  private String registerAndLogin(String email) {
    registerUser(email);
    jdbcTemplate.update(
        "update users set updated_at = ? where email = ?", Timestamp.from(REGISTERED_AT), email);
    return loginToken(email);
  }

  private RestTestClient.ResponseSpec putMe(String token, Map<String, Object> body) {
    return restTestClient
        .put()
        .uri("/users/me")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .exchange();
  }

  private Map<String, Object> row(String email) {
    return jdbcTemplate.queryForMap(
        "select last_name, first_name, updated_at from users where email = ?", email);
  }

  @Test
  void should_return_200_and_save_the_new_name_with_the_clock_as_update_date() {
    String token = registerAndLogin("patch-ca1@mail.fr");

    putMe(token, Map.of("lastName", "Martin", "firstName", "Paul"))
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.lastName")
        .isEqualTo("Martin")
        .jsonPath("$.firstName")
        .isEqualTo("Paul")
        .jsonPath("$.email")
        .isEqualTo("patch-ca1@mail.fr")
        .jsonPath("$.updatedAt")
        .isEqualTo("2025-06-15T10:00:00Z");

    Map<String, Object> row = row("patch-ca1@mail.fr");
    assertThat(row.get("last_name")).isEqualTo("Martin");
    assertThat(row.get("first_name")).isEqualTo("Paul");
    assertThat(((Timestamp) row.get("updated_at")).toInstant())
        .isEqualTo(FixedClockConfiguration.NOW);
  }

  @Test
  void should_return_400_with_the_field_errors_and_change_nothing_when_a_name_is_invalid() {
    String token = registerAndLogin("patch-ca2@mail.fr");

    putMe(token, Map.of("lastName", "Martin", "firstName", " "))
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("firstName")
        .jsonPath("$.errors[0].message")
        .isEqualTo("First name cannot be blank");

    Map<String, Object> row = row("patch-ca2@mail.fr");
    assertThat(row.get("last_name")).isEqualTo("Dupont");
    assertThat(row.get("first_name")).isEqualTo("Jean");
    assertThat(((Timestamp) row.get("updated_at")).toInstant()).isEqualTo(REGISTERED_AT);
  }

  @Test
  void should_return_200_and_keep_the_update_date_when_the_names_are_the_current_ones() {
    String token = registerAndLogin("patch-ca3@mail.fr");

    putMe(token, Map.of("lastName", "Dupont", "firstName", "Jean"))
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.updatedAt")
        .isEqualTo("2025-01-01T08:00:00Z");

    assertThat(((Timestamp) row("patch-ca3@mail.fr").get("updated_at")).toInstant())
        .isEqualTo(REGISTERED_AT);
  }

  @Test
  void should_change_only_the_name_of_the_token_owner_when_the_body_has_other_fields() {
    String token = registerAndLogin("patch-ca4@mail.fr");
    registerAndLogin("patch-ca4-other@mail.fr");
    String ownerId =
        jdbcTemplate.queryForObject(
            "select cast(id as varchar) from users where email = ?",
            String.class,
            "patch-ca4@mail.fr");

    putMe(
            token,
            Map.of(
                "id", "11111111-1111-1111-1111-111111111111",
                "lastName", "Martin",
                "firstName", "Paul",
                "email", "hacked@mail.fr",
                "birthDate", "2000-01-01",
                "gender", "FEMALE"))
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.id")
        .isEqualTo(ownerId)
        .jsonPath("$.email")
        .isEqualTo("patch-ca4@mail.fr")
        .jsonPath("$.birthDate")
        .isEqualTo("1990-05-17")
        .jsonPath("$.gender")
        .isEqualTo("MALE");

    Map<String, Object> other = row("patch-ca4-other@mail.fr");
    assertThat(other.get("last_name")).isEqualTo("Dupont");
    assertThat(other.get("first_name")).isEqualTo("Jean");
  }

  @Test
  void should_return_400_on_last_name_and_change_nothing_when_it_is_absent() {
    String token = registerAndLogin("patch-ca5@mail.fr");

    putMe(token, Map.of("firstName", "Paul"))
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("lastName");

    Map<String, Object> row = row("patch-ca5@mail.fr");
    assertThat(row.get("first_name")).isEqualTo("Jean");
  }

  @Test
  void should_return_400_on_first_name_when_it_is_absent() {
    String token = registerAndLogin("patch-ca6@mail.fr");

    putMe(token, Map.of("lastName", "Martin"))
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("firstName");
  }
}
