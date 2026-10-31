package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.ChangeEmail;
import fr.zbar.showtime.backend.core.application.ChangePassword;
import fr.zbar.showtime.backend.core.application.GetProfile;
import fr.zbar.showtime.backend.core.application.UpdateProfile;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {

  private final GetProfile getProfile;
  private final UpdateProfile updateProfile;
  private final ChangeEmail changeEmail;
  private final ChangePassword changePassword;

  @Operation(
      summary = "Get the profile of the token owner",
      description =
          "Logout is client-side only: the client forgets its token, the server keeps no session.")
  @GetMapping("/me")
  UserResponse me(@AuthenticationPrincipal Jwt jwt) {
    return UserResponse.from(getProfile.execute(userId(jwt)));
  }

  @Operation(summary = "Update the name of the token owner")
  @PutMapping("/me")
  UserResponse updateMe(
      @AuthenticationPrincipal Jwt jwt, @RequestBody UpdateProfileRequest request) {
    return UserResponse.from(updateProfile.execute(request.toCommand(userId(jwt))));
  }

  @Operation(summary = "Change the email of the token owner")
  @PutMapping("/me/email")
  UserResponse changeEmail(
      @AuthenticationPrincipal Jwt jwt, @RequestBody ChangeEmailRequest request) {
    return UserResponse.from(changeEmail.execute(request.toCommand(userId(jwt))));
  }

  @Operation(summary = "Change the password of the token owner")
  @PutMapping("/me/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void changePassword(
      @AuthenticationPrincipal Jwt jwt, @RequestBody ChangePasswordRequest request) {
    changePassword.execute(request.toCommand(userId(jwt)));
  }

  private static UUID userId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
