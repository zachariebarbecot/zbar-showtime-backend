package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.ChangePasswordCommand;
import java.util.UUID;

public record ChangePasswordRequest(String currentPassword, String newPassword) {

  ChangePasswordCommand toCommand(UUID userId) {
    return new ChangePasswordCommand(userId, currentPassword, newPassword);
  }
}
