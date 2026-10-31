package fr.zbar.showtime.backend.core.user.application;

import fr.zbar.showtime.backend.core.user.domain.User;

public interface ChangeEmail {

  User execute(ChangeEmailCommand command);
}
