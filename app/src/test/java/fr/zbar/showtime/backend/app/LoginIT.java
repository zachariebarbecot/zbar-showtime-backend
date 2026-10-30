package fr.zbar.showtime.backend.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.client.RestTestClient;

class LoginIT extends AbstractIT {

  private static final String PASSWORD = "Str0ng!Passw0rd";

  @Autowired private JdbcTemplate jdbcTemplate;

  @Value("${app.jwt.secret}")
  private String jwtSecret;

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

  private String userId(String email) {
    return jdbcTemplate.queryForObject(
        "select cast(id as varchar) from users where email = ?", String.class, email);
  }

  private RestTestClient.ResponseSpec login(String email, String password) {
    return restTestClient
        .post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("email", email, "password", password))
        .exchange();
  }

  @Test
  void should_return_200_and_signed_jwt_valid_one_hour_when_credentials_are_correct()
      throws Exception {
    register("login-ca1@mail.fr");

    String body =
        new String(
            login("login-ca1@mail.fr", PASSWORD)
                .expectStatus()
                .isOk()
                .expectBody()
                .returnResult()
                .getResponseBody(),
            StandardCharsets.UTF_8);

    String[] parts = JsonPath.<String>read(body, "$.token").split("\\.");
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] expectedSignature =
        mac.doFinal((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
    assertThat(MessageDigest.isEqual(expectedSignature, Base64.getUrlDecoder().decode(parts[2])))
        .isTrue();
    String claims = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    long issuedAt = ((Number) JsonPath.read(claims, "$.iat")).longValue();
    long expiresAt = ((Number) JsonPath.read(claims, "$.exp")).longValue();
    assertThat(expiresAt - issuedAt).isEqualTo(3600);
    assertThat(JsonPath.<String>read(claims, "$.sub")).isEqualTo(userId("login-ca1@mail.fr"));
  }

  @Test
  void should_return_401_without_token_when_password_is_wrong() {
    register("login-ca2@mail.fr");

    login("login-ca2@mail.fr", "Wr0ng!Passw0rd")
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Identifiants invalides")
        .jsonPath("$.token")
        .doesNotExist();
  }

  @Test
  void should_return_same_response_as_wrong_password_when_no_account_has_the_email() {
    register("login-ca3@mail.fr");

    byte[] wrongPassword =
        login("login-ca3@mail.fr", "Wr0ng!Passw0rd")
            .expectStatus()
            .isUnauthorized()
            .expectBody()
            .returnResult()
            .getResponseBody();
    byte[] unknownEmail =
        login("nobody-ca3@mail.fr", "Wr0ng!Passw0rd")
            .expectStatus()
            .isUnauthorized()
            .expectBody()
            .returnResult()
            .getResponseBody();

    assertThat(new String(unknownEmail, StandardCharsets.UTF_8))
        .isEqualTo(new String(wrongPassword, StandardCharsets.UTF_8));
  }

  @Test
  void should_log_in_when_email_has_spaces_and_uppercase() {
    register("jean-ca4@mail.fr");

    login(" Jean-CA4@Mail.FR ", PASSWORD).expectStatus().isOk();
  }

  @Test
  void should_expose_only_the_token_when_login_succeeds() {
    register("login-ca5@mail.fr");

    login("login-ca5@mail.fr", PASSWORD)
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.password")
        .doesNotExist()
        .jsonPath("$.passwordHash")
        .doesNotExist()
        .jsonPath("$.length()")
        .isEqualTo(1);
  }
}
