package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.GetProfile;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {

  private final GetProfile getProfile;

  @Operation(
      summary = "Get the profile of the token owner",
      description =
          "Logout is client-side only: the client forgets its token, the server keeps no session.")
  @GetMapping("/me")
  UserResponse me(@AuthenticationPrincipal Jwt jwt) {
    return UserResponse.from(getProfile.execute(UUID.fromString(jwt.getSubject())));
  }
}
