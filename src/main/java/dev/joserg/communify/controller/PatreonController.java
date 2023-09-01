package dev.joserg.communify.controller;

import dev.joserg.communify.data.ConnectionDetails;
import dev.joserg.communify.entity.UserActor;
import dev.joserg.communify.entity.UserPatreonAccessToken;
import dev.joserg.communify.repository.UserActorRepository;
import dev.joserg.communify.repository.UserPatreonAccessTokenRepository;
import dev.joserg.communify.service.PatreonService;
import dev.joserg.communify.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PatreonController {

    private final UserPatreonAccessTokenRepository userPatreonAccessTokenRepository;
    private final UserActorRepository userActorRepository;
    private final PatreonService patreonService;
    private final UserService userService;

    @GetMapping("/private/patreon")
    public ResponseEntity<?> patreon(@RequestParam("code") String code, JwtAuthenticationToken principal) {
        final var userEmail = this.userService.getUserEmailFromPrincipal(principal);
        if (userEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User email not found"));
        }

        final var user = userActorRepository.save(
                userActorRepository.findByUserEmail(userEmail.get())
                        .map(existingUser -> existingUser.setUserEmail(userEmail.get()))
                        .orElseGet(() -> UserActor.builder().userEmail(userEmail.get()).build()));

        final var patreonAccessToken = this.patreonService.acquirePatreonAccessToken(code);

        if (patreonAccessToken.isEmpty()) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Could not acquire token"));
        }

        this.userPatreonAccessTokenRepository.save(
                userPatreonAccessTokenRepository.findByUserId(user.getId())
                        .map(userToken -> userToken
                                .setUserEmail(user.getUserEmail())
                                .setPatreonAccessToken(patreonAccessToken.get()))
                        .orElseGet(() -> UserPatreonAccessToken.builder()
                                .userId(user.getId())
                                .userEmail(user.getUserEmail())
                                .patreonAccessToken(patreonAccessToken.get())
                                .build()));

        return ResponseEntity.ok(Map.of("token", "acquired"));
    }

    @GetMapping("/private/connection-details")
    private ResponseEntity<?> connectionDetails(JwtAuthenticationToken principal) {
        final var userEmail = this.userService.getUserEmailFromPrincipal(principal);
        if (userEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                userActorRepository.findByUserEmail(userEmail.get())
                        .flatMap(user -> userPatreonAccessTokenRepository.findByUserId(user.getId()))
                        .map(patreonToken -> ConnectionDetails.builder().patreonIsConnected(true).build())
                        .orElseGet(() -> ConnectionDetails.builder().patreonIsConnected(false).build()));
    }

    @GetMapping("/private/patreon/account-info")
    public ResponseEntity<?> patreonAccountInfo(JwtAuthenticationToken principal) {
        final var userEmail = this.userService.getUserEmailFromPrincipal(principal);
        if (userEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User email not found"));
        }

        final var patreonAccessToken = this.userPatreonAccessTokenRepository.findByUserEmail(userEmail.get());
        if (patreonAccessToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final var accountInfo = this.patreonService.ofUserPatreonAccessToken(patreonAccessToken.get());

        return ResponseEntity.ok(accountInfo);
    }

}
