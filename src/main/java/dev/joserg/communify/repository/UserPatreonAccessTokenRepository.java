package dev.joserg.communify.repository;

import dev.joserg.communify.entity.UserPatreonAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPatreonAccessTokenRepository extends JpaRepository<UserPatreonAccessToken, Long> {

    Optional<UserPatreonAccessToken> findByUserId(Long id);

    Optional<UserPatreonAccessToken> findByUserEmail(String email);

}
