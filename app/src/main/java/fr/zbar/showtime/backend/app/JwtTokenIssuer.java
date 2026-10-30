package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.domain.TokenIssuer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JwtTokenIssuer implements TokenIssuer {

  private static final Duration VALIDITY = Duration.ofHours(1);

  private final JwtEncoder jwtEncoder;
  private final Clock clock;

  @Override
  public String issue(UUID userId) {
    Instant now = clock.instant();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiresAt(now.plus(VALIDITY))
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
