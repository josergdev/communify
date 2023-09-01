package dev.joserg.communify.controller;

import dev.joserg.communify.data.ImprovementClaimRequest;
import dev.joserg.communify.entity.ImprovementClaim;
import dev.joserg.communify.repository.ImprovementClaimRepository;
import dev.joserg.communify.repository.UserPatreonAccessTokenRepository;
import dev.joserg.communify.service.PatreonService;
import dev.joserg.communify.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class ImprovementClaimController {

    private final UserPatreonAccessTokenRepository userPatreonAccessTokenRepository;
    private final ImprovementClaimRepository improvementClaimRepository;
    private final PatreonService patreonService;
    private final UserService userService;

    @PostMapping(value = "/private/improvement-claims")
    public ResponseEntity<?> addTierImprovement(JwtAuthenticationToken principal, @Valid @RequestBody ImprovementClaimRequest request) {
        final var userEmail = this.userService.getUserEmailFromPrincipal(principal);
        if (userEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User email not found"));
        }

        final var patreonAccessToken = this.userPatreonAccessTokenRepository.findByUserEmail(userEmail.get());
        if (patreonAccessToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final var accountInfo = this.patreonService.ofUserPatreonAccessToken(patreonAccessToken.get());

        final var entitledTiers = accountInfo.getCurrentlyEntitledTiers();
        final var entitledTier = entitledTiers.stream()
                .filter(et -> Objects.equals(et.getParentCampaign().getId(), request.getCampaignId()) && Objects.equals(et.getTier().getId(), request.getTierId()))
                .findFirst();
        if (entitledTier.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final var claim = this.improvementClaimRepository.save(
                this.improvementClaimRepository.findByClaimerEmailAndCampaignIdAndTierId(userEmail.get(), request.getCampaignId(), request.getTierId())
                        .map(existingClaim -> existingClaim.setTelegramUsername(request.getTelegramUsername()))
                        .orElseGet(() -> ImprovementClaim.builder()
                                .claimerEmail(userEmail.get())
                                .campaignId(entitledTier.get().getParentCampaign().getId())
                                .tierId(entitledTier.get().getTier().getId())
                                .telegramUsername(request.getTelegramUsername())
                                .build()));

        return ResponseEntity.ok(claim);
    }

    @GetMapping(value = "/private/improvement-claims")
    public ResponseEntity<?> improvementClaims(JwtAuthenticationToken principal) {
        final var userEmail = this.userService.getUserEmailFromPrincipal(principal);
        if (userEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User email not found"));
        }

        final var claims = this.improvementClaimRepository.findByClaimerEmail(userEmail.get());

        return ResponseEntity.ok(Map.of("improvementClaims", claims));
    }

    @DeleteMapping(value = "/private/improvement-claims")
    public ResponseEntity<?> deleteImprovementClaim(JwtAuthenticationToken principal, @RequestParam Long campaignId, @RequestParam Long tierId) {
        final var userEmail = this.userService.getUserEmailFromPrincipal(principal);
        if (userEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User email not found"));
        }

        this.improvementClaimRepository.findByClaimerEmailAndCampaignIdAndTierId(userEmail.get(), campaignId, tierId)
                .ifPresent(this.improvementClaimRepository::delete);

        return ResponseEntity.ok().build();
    }

}
