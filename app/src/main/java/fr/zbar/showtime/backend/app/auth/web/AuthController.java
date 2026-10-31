package fr.zbar.showtime.backend.app.auth.web;

import fr.zbar.showtime.backend.core.auth.application.AuthenticateUser;
import fr.zbar.showtime.backend.core.user.application.RegisterUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
class AuthController {

  private final RegisterUser registerUser;
  private final AuthenticateUser authenticateUser;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  void register(@RequestBody RegisterRequest request) {
    registerUser.execute(request.toCommand());
  }

  @PostMapping("/login")
  LoginResponse login(@RequestBody LoginRequest request) {
    return new LoginResponse(authenticateUser.execute(request.toCommand()));
  }
}
