package fr.zbar.showtime.backend.app.auth.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface LoginJpaRepository extends JpaRepository<LoginJpaEntity, UUID> {}
