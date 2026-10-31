package fr.zbar.showtime.backend.app;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfigureMockMvc
class UsersMeIT extends AbstractIT {

  private static final String PASSWORD = "Str0ng!Passw0rd";

  @Autowired private JdbcTemplate jdbcTemplate;

  private void register(String email) {
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
  }

  private String loginToken(String email) {
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

  @Test
  void should_return_200_and_the_profile_without_password_when_the_token_is_valid() {
    register("me-ca1@mail.fr");
    String userId =
        jdbcTemplate.queryForObject(
            "select cast(id as varchar) from users where email = ?",
            String.class,
            "me-ca1@mail.fr");

    restTestClient
        .get()
        .uri("/users/me")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken("me-ca1@mail.fr"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.id")
        .isEqualTo(userId)
        .jsonPath("$.lastName")
        .isEqualTo("Dupont")
        .jsonPath("$.firstName")
        .isEqualTo("Jean")
        .jsonPath("$.email")
        .isEqualTo("me-ca1@mail.fr")
        .jsonPath("$.birthDate")
        .isEqualTo("1990-05-17")
        .jsonPath("$.gender")
        .isEqualTo("MALE")
        .jsonPath("$.createdAt")
        .isEqualTo("2025-06-15T10:00:00Z")
        .jsonPath("$.updatedAt")
        .isEqualTo("2025-06-15T10:00:00Z")
        .jsonPath("$.password")
        .doesNotExist()
        .jsonPath("$.passwordHash")
        .doesNotExist()
        .jsonPath("$.length()")
        .isEqualTo(8);
  }
}
