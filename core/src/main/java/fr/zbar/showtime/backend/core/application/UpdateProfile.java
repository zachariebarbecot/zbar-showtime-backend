package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.User;

public interface UpdateProfile {

  User execute(UpdateProfileCommand command);
}
