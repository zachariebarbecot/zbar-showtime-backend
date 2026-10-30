package fr.zbar.showtime.backend.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.client.RestTestClient;

class AuthControllerIT extends AbstractIT {

  private static final String PASSWORD = "Str0ng!Passw0rd";

  @Autowired private JdbcTemplate jdbcTemplate;

  private Map<String, Object> registration(String email) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("lastName", "Dupont");
    body.put("firstName", "Jean");
    body.put("email", email);
    body.put("password", PASSWORD);
    body.put("birthDate", "1990-05-17");
    body.put("gender", "HOMME");
    return body;
  }

  private RestTestClient.ResponseSpec register(Map<String, Object> body) {
    return restTestClient
        .post()
        .uri("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .exchange();
  }

  @Test
  void should_create_account_and_hide_password_when_registration_is_valid() {
    register(registration("ca1@b.fr"))
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.id")
        .isNotEmpty()
        .jsonPath("$.email")
        .isEqualTo("ca1@b.fr")
        .jsonPath("$.password")
        .doesNotExist()
        .jsonPath("$.passwordHash")
        .doesNotExist();
  }

  @Test
  void should_return_same_creation_and_update_dates_when_registration_is_valid() {
    Map<String, Object> response =
        register(registration("ca1-dates@b.fr"))
            .expectStatus()
            .isCreated()
            .expectBody(
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(response).containsKeys("createdAt", "updatedAt");
    assertThat(response.get("createdAt")).isEqualTo(response.get("updatedAt"));
  }

  @Test
  void should_store_bcrypt_hash_different_from_clear_password_when_registration_is_valid() {
    register(registration("ca1-hash@b.fr")).expectStatus().isCreated();

    String storedHash =
        jdbcTemplate.queryForObject(
            "select password_hash from users where email = ?", String.class, "ca1-hash@b.fr");
    assertThat(storedHash).startsWith("$2").isNotEqualTo(PASSWORD);
  }
}
