package fr.zbar.showtime.backend.app;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class ChangeEmailIT extends AbstractIT {

  private static final String PASSWORD = "Str0ng!Passw0rd";

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

  private RestTestClient.ResponseSpec login(String email) {
    return restTestClient
        .post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("email", email, "password", PASSWORD))
        .exchange();
  }

  private String token(String email) {
    byte[] body = login(email).expectStatus().isOk().expectBody().returnResult().getResponseBody();
    return JsonPath.read(new String(body, StandardCharsets.UTF_8), "$.token");
  }

  private RestTestClient.ResponseSpec changeEmail(
      String token, String currentPassword, String newEmail) {
    return restTestClient
        .put()
        .uri("/users/me/email")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("currentPassword", currentPassword, "newEmail", newEmail))
        .exchange();
  }

  @Test
  void should_return_200_and_switch_login_to_the_new_email_when_the_password_is_correct() {
    register("chg-ca1@mail.fr");
    String token = token("chg-ca1@mail.fr");

    changeEmail(token, PASSWORD, "  New-Chg-Ca1@Mail.FR ")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.email")
        .isEqualTo("new-chg-ca1@mail.fr");

    login("new-chg-ca1@mail.fr").expectStatus().isOk();
    login("chg-ca1@mail.fr")
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Identifiants invalides");
  }

  @Test
  void should_return_401_and_keep_the_email_when_the_current_password_is_wrong_even_if_taken() {
    register("chg-ca2@mail.fr");
    register("chg-ca2-taken@mail.fr");
    String token = token("chg-ca2@mail.fr");

    changeEmail(token, "Wr0ng!Passw0rd", "chg-ca2-taken@mail.fr")
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Identifiants invalides");

    login("chg-ca2@mail.fr").expectStatus().isOk();
  }

  @Test
  void should_return_409_and_change_nothing_when_the_email_is_used_after_normalization() {
    register("chg-ca3@mail.fr");
    register("chg-ca3-taken@mail.fr");
    String token = token("chg-ca3@mail.fr");

    changeEmail(token, PASSWORD, " CHG-CA3-Taken@Mail.fr ").expectStatus().isEqualTo(409);

    login("chg-ca3@mail.fr").expectStatus().isOk();
    login("chg-ca3-taken@mail.fr").expectStatus().isOk();
  }

  @Test
  void should_return_400_with_field_and_change_nothing_when_the_new_email_is_invalid() {
    register("chg-ca4@mail.fr");
    String token = token("chg-ca4@mail.fr");

    changeEmail(token, PASSWORD, "not-an-email")
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("email")
        .jsonPath("$.errors[0].message")
        .isEqualTo("Email must be a valid address");

    login("chg-ca4@mail.fr").expectStatus().isOk();
  }

  @Test
  void should_return_400_with_field_and_change_nothing_when_the_current_password_is_absent() {
    register("chg-ca4b@mail.fr");
    String token = token("chg-ca4b@mail.fr");

    restTestClient
        .put()
        .uri("/users/me/email")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("newEmail", "chg-ca4b-new@mail.fr"))
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("currentPassword");

    login("chg-ca4b@mail.fr").expectStatus().isOk();
  }

  @Test
  void should_return_200_when_the_email_is_the_same_after_normalization() {
    register("chg-ca5@mail.fr");
    String token = token("chg-ca5@mail.fr");

    changeEmail(token, PASSWORD, " CHG-CA5@Mail.fr ")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.email")
        .isEqualTo("chg-ca5@mail.fr");
  }
}
