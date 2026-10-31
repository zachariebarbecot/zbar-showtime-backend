package fr.zbar.showtime.backend.core.user.application;

import fr.zbar.showtime.backend.core.user.domain.User;
import java.util.UUID;

public interface GetProfile {

  User execute(UUID userId);
}
