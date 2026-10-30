package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.User;

public interface RegisterUser {

  User execute(RegisterUserCommand command);
}
