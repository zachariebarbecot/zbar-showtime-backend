package fr.zbar.showtime.backend.core.user.application;

import java.util.UUID;

public record ChangePasswordCommand(UUID userId, String currentPassword, String newPassword) {}
