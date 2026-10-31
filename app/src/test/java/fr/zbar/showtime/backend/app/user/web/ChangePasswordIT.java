package fr.zbar.showtime.backend.app.user.web;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class ChangePasswordIT extends AbstractIT {

  private static final String NEW_PASSWORD = "N3w!Passw0rdXyz";

  private RestTestClient.ResponseSpec login(String email, String password) {
    return restTestClient
        .post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("email", email, "password", password))
        .exchange();
  }

  private RestTestClient.ResponseSpec changePassword(
      String token, String currentPassword, String newPassword) {
    return restTestClient
        .put()
        .uri("/users/me/password")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("currentPassword", currentPassword, "newPassword", newPassword))
        .exchange();
  }

  @Test
  void should_return_204_and_accept_only_the_new_password_when_current_password_is_correct() {
    registerUser("pwd-ca1@mail.fr");

    changePassword(loginToken("pwd-ca1@mail.fr"), PASSWORD, NEW_PASSWORD)
        .expectStatus()
        .isNoContent();

    login("pwd-ca1@mail.fr", NEW_PASSWORD).expectStatus().isOk();
    login("pwd-ca1@mail.fr", PASSWORD)
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Invalid credentials");
  }

  @Test
  void should_return_401_and_keep_the_password_when_current_password_is_wrong() {
    registerUser("pwd-ca2@mail.fr");

    changePassword(loginToken("pwd-ca2@mail.fr"), "Wr0ng!Passw0rd", NEW_PASSWORD)
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Invalid credentials");

    login("pwd-ca2@mail.fr", PASSWORD).expectStatus().isOk();
    login("pwd-ca2@mail.fr", NEW_PASSWORD).expectStatus().isUnauthorized();
  }

  @Test
  void should_return_400_on_new_password_and_keep_the_password_when_it_is_not_compliant() {
    registerUser("pwd-ca3@mail.fr");

    changePassword(loginToken("pwd-ca3@mail.fr"), PASSWORD, "short")
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("newPassword")
        .jsonPath("$.errors[0].message")
        .isNotEmpty();

    login("pwd-ca3@mail.fr", PASSWORD).expectStatus().isOk();
  }

  @Test
  void should_return_400_and_keep_the_password_when_new_password_equals_the_current_one() {
    registerUser("pwd-ca4@mail.fr");

    changePassword(loginToken("pwd-ca4@mail.fr"), PASSWORD, PASSWORD)
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("newPassword");

    login("pwd-ca4@mail.fr", PASSWORD).expectStatus().isOk();
  }

  @Test
  void should_keep_the_token_issued_before_the_change_valid() {
    registerUser("pwd-ca5@mail.fr");
    String token = loginToken("pwd-ca5@mail.fr");
    changePassword(token, PASSWORD, NEW_PASSWORD).expectStatus().isNoContent();

    restTestClient
        .get()
        .uri("/users/me")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void should_return_400_on_new_password_when_it_is_missing() {
    registerUser("pwd-missing-new@mail.fr");

    restTestClient
        .put()
        .uri("/users/me/password")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken("pwd-missing-new@mail.fr"))
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("currentPassword", PASSWORD))
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("newPassword");
  }

  @Test
  void should_return_400_on_current_password_when_it_is_missing() {
    registerUser("pwd-missing-cur@mail.fr");

    restTestClient
        .put()
        .uri("/users/me/password")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken("pwd-missing-cur@mail.fr"))
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("newPassword", NEW_PASSWORD))
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("currentPassword");
  }
}
