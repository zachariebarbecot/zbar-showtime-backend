package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.ChangeEmailCommand;
import java.util.UUID;

public record ChangeEmailRequest(String currentPassword, String newEmail) {

  ChangeEmailCommand toCommand(UUID userId) {
    return new ChangeEmailCommand(userId, currentPassword, newEmail);
  }
}
