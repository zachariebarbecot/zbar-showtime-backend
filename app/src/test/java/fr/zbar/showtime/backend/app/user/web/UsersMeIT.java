package fr.zbar.showtime.backend.app.user.web;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.app.support.FixedClockConfiguration;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class UsersMeIT extends AbstractIT {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  private String token(String secret, String subject, Instant issuedAt, Duration validity) {
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(subject)
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plus(validity))
            .build();
    return new NimbusJwtEncoder(
            new ImmutableSecret<>(
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")))
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }

  private RestTestClient.ResponseSpec getMe(String authorization) {
    return restTestClient
        .get()
        .uri("/users/me")
        .header(HttpHeaders.AUTHORIZATION, authorization)
        .exchange();
  }

  @Test
  void should_return_200_and_the_profile_without_password_when_the_token_is_valid() {
    registerUser("me-ca1@mail.fr");
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

  @Test
  void should_return_401_when_there_is_no_token() {
    restTestClient.get().uri("/users/me").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void should_return_401_when_the_token_is_expired() {
    String expired =
        token(
            jwtSecret,
            UUID.randomUUID().toString(),
            FixedClockConfiguration.NOW.minus(Duration.ofHours(3)),
            Duration.ofHours(1));

    getMe("Bearer " + expired).expectStatus().isUnauthorized();
  }

  @Test
  void should_return_401_when_the_token_is_signed_with_another_secret() {
    String forged =
        token(
            "another-secret-of-at-least-32-bytes!!",
            UUID.randomUUID().toString(),
            FixedClockConfiguration.NOW,
            Duration.ofHours(1));

    getMe("Bearer " + forged).expectStatus().isUnauthorized();
  }

  @Test
  void should_return_401_when_the_token_is_malformed() {
    getMe("Bearer not.a.jwt").expectStatus().isUnauthorized();
  }

  @Test
  void should_return_401_when_the_token_subject_matches_no_user() {
    String unknownUser =
        token(
            jwtSecret,
            UUID.randomUUID().toString(),
            FixedClockConfiguration.NOW,
            Duration.ofHours(1));

    getMe("Bearer " + unknownUser).expectStatus().isUnauthorized();
  }

  @Test
  void should_keep_register_and_login_accessible_without_token() {
    registerUser("me-ca5@mail.fr");

    loginToken("me-ca5@mail.fr");
  }
}
