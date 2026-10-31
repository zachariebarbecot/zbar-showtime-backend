package fr.zbar.showtime.backend.core.user.application;

import java.util.UUID;

public record ChangeEmailCommand(UUID userId, String currentPassword, String newEmail) {}
