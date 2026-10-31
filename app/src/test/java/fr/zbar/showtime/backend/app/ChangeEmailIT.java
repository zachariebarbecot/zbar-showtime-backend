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
}
