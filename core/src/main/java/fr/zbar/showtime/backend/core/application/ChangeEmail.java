package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.User;

public interface ChangeEmail {

  User execute(ChangeEmailCommand command);
}
