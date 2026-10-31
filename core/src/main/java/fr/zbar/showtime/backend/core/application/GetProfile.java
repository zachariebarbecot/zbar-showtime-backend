package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.User;
import java.util.UUID;

public interface GetProfile {

  User execute(UUID userId);
}
