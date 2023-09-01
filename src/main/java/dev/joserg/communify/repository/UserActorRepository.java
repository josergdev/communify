package dev.joserg.communify.repository;

import dev.joserg.communify.entity.UserActor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserActorRepository extends JpaRepository<UserActor, Long> {

    Optional<UserActor> findByUserEmail(String email);
}
