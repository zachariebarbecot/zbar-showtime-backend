package fr.zbar.showtime.backend.app.user.web;

import fr.zbar.showtime.backend.core.user.application.ChangeEmailCommand;
import java.util.UUID;

public record ChangeEmailRequest(String currentPassword, String newEmail) {

  ChangeEmailCommand toCommand(UUID userId) {
    return new ChangeEmailCommand(userId, currentPassword, newEmail);
  }
}
