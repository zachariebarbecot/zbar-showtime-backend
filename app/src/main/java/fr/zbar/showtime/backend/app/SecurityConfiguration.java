package fr.zbar.showtime.backend.app;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfiguration {

  private static final int MIN_SECRET_BYTES = 32;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/auth/**",
                        "/hello",
                        "/actuator/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .build();
  }

  @Bean
  JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String secret) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(secret)));
  }

  @Bean
  JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret, Clock clock) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(secret)).build();
    JwtTimestampValidator timestampValidator = new JwtTimestampValidator();
    timestampValidator.setClock(clock);
    decoder.setJwtValidator(timestampValidator);
    return decoder;
  }

  private static SecretKeySpec secretKey(String secret) {
    byte[] key = secret.getBytes(StandardCharsets.UTF_8);
    if (key.length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "app.jwt.secret must be at least %d bytes in UTF-8".formatted(MIN_SECRET_BYTES));
    }
    return new SecretKeySpec(key, "HmacSHA256");
  }
}
