package fr.zbar.showtime.backend.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
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

  private static final String PASSWORD = "Str0ng!Passw0rd";
  private static final Instant REGISTERED_AT = Instant.parse("2025-01-01T08:00:00Z");

  @Autowired private JdbcTemplate jdbcTemplate;

  private String registerAndLogin(String email) {
    restTestClient
        .post()
        .uri("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Map.of(
                "lastName", "Dupont",
                "firstName", "Jean",
                "email", email,
                "password", PASSWORD,
                "birthDate", "1990-05-17",
                "gender", "MALE"))
        .exchange()
        .expectStatus()
        .isCreated();
    jdbcTemplate.update(
        "update users set updated_at = ? where email = ?", Timestamp.from(REGISTERED_AT), email);
    byte[] body =
        restTestClient
            .post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("email", email, "password", PASSWORD))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .returnResult()
            .getResponseBody();
    return JsonPath.read(new String(body, StandardCharsets.UTF_8), "$.token");
  }

  private RestTestClient.ResponseSpec patchMe(String token, Map<String, Object> body) {
    return restTestClient
        .patch()
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

    patchMe(token, Map.of("lastName", "Martin", "firstName", "Paul"))
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

    patchMe(token, Map.of("lastName", "Martin", "firstName", " "))
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

    patchMe(token, Map.of("lastName", "Dupont", "firstName", "Jean"))
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.updatedAt")
        .isEqualTo("2025-01-01T08:00:00Z");

    assertThat(((Timestamp) row("patch-ca3@mail.fr").get("updated_at")).toInstant())
        .isEqualTo(REGISTERED_AT);
  }
}
