package fr.zbar.showtime.backend.app;

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
  void should_return_200_and_accept_only_the_new_password_when_current_password_is_correct() {
    registerUser("pwd-ca1@mail.fr");

    changePassword(loginToken("pwd-ca1@mail.fr"), PASSWORD, NEW_PASSWORD).expectStatus().isOk();

    login("pwd-ca1@mail.fr", NEW_PASSWORD).expectStatus().isOk();
    login("pwd-ca1@mail.fr", PASSWORD)
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Identifiants invalides");
  }
}
