package fr.zbar.showtime.backend.core.user.application;

import fr.zbar.showtime.backend.core.user.domain.User;

public interface UpdateProfile {

  User execute(UpdateProfileCommand command);
}
