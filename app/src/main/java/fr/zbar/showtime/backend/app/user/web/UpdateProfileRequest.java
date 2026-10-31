package fr.zbar.showtime.backend.app.user.web;

import fr.zbar.showtime.backend.core.user.application.UpdateProfileCommand;
import java.util.UUID;

public record UpdateProfileRequest(String lastName, String firstName) {

  UpdateProfileCommand toCommand(UUID userId) {
    return new UpdateProfileCommand(userId, lastName, firstName);
  }
}
