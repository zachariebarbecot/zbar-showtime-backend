package fr.zbar.showtime.backend.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

  @Test
  void should_return_400_listing_every_violated_rule_and_create_no_account_when_password_is_weak() {
    Map<String, Object> body = registration("ca2@b.fr");
    body.put("password", "ca2@b.fr");

    register(body)
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[?(@.field == 'password')].message")
        .value(
            messages ->
                assertThat(((java.util.List<?>) messages).stream().map(Object::toString).toList())
                    .contains(
                        "Password must be at least 12 characters",
                        "Password must contain an uppercase letter",
                        "Password must not contain the email"));
    assertThat(countAccounts("ca2@b.fr")).isZero();
  }

  @Test
  void should_return_409_and_keep_a_single_account_when_email_is_already_used() {
    register(registration("ca3@b.fr")).expectStatus().isCreated();

    register(registration("ca3@b.fr"))
        .expectStatus()
        .isEqualTo(409)
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Email already used");
    assertThat(countAccounts("ca3@b.fr")).isEqualTo(1);
  }

  @Test
  void should_create_a_single_account_when_same_email_is_registered_concurrently()
      throws Exception {
    int attempts = 5;
    try (ExecutorService executor = Executors.newFixedThreadPool(attempts)) {
      List<Future<Integer>> statuses = new ArrayList<>();
      for (int i = 0; i < attempts; i++) {
        statuses.add(
            executor.submit(
                () -> register(registration("ca3-race@b.fr")).returnResult().getStatus().value()));
      }
      List<Integer> results = new ArrayList<>();
      for (Future<Integer> status : statuses) {
        results.add(status.get());
      }

      assertThat(results).containsOnly(201, 409).filteredOn(code -> code == 201).hasSize(1);
    }
    assertThat(countAccounts("ca3-race@b.fr")).isEqualTo(1);
  }

  @ParameterizedTest
  @CsvSource({
    "email, email, not-an-email",
    "birthDate, birthDate, 2999-01-01",
    "gender, gender, AUTRE",
    "lastName, lastName, ''",
    "firstName, firstName, ''"
  })
  void should_return_400_naming_the_field_and_create_no_account_when_data_is_invalid(
      String field, String key, String value) {
    Map<String, Object> body = registration("ca4-" + field + "@b.fr");
    body.put(key, value);

    register(body)
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo(field);
    assertThat(countAccounts("ca4-" + field + "@b.fr")).isZero();
  }

  @Test
  void should_return_400_when_birth_date_is_today() {
    Map<String, Object> body = registration("ca4-today@b.fr");
    body.put("birthDate", LocalDate.now(ZoneOffset.UTC).toString());

    register(body)
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("birthDate");
    assertThat(countAccounts("ca4-today@b.fr")).isZero();
  }

  @Test
  void should_return_400_when_last_name_is_missing() {
    Map<String, Object> body = registration("ca4-missing@b.fr");
    body.remove("lastName");

    register(body)
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("lastName");
    assertThat(countAccounts("ca4-missing@b.fr")).isZero();
  }

  private Integer countAccounts(String email) {
    return jdbcTemplate.queryForObject(
        "select count(*) from users where email = ?", Integer.class, email);
  }
}
