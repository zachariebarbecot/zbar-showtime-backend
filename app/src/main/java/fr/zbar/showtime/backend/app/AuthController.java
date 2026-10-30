package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.RegisterUser;
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

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  void register(@RequestBody RegisterRequest request) {
    registerUser.execute(request.toCommand());
  }
}
