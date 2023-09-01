package dev.joserg.communify.controller;

import dev.joserg.communify.data.TierImprovementRequest;
import dev.joserg.communify.entity.TierImprovement;
import dev.joserg.communify.repository.TierImprovementRepository;
import dev.joserg.communify.repository.UserPatreonAccessTokenRepository;
import dev.joserg.communify.service.PatreonService;
import dev.joserg.communify.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class TierImprovementController {

    private final UserPatreonAccessTokenRepository userPatreonAccessTokenRepository;
    private final TierImprovementRepository tierImprovementRepository;
    private final PatreonService patreonService;
    private final UserService userService;

    @PostMapping(value = "/private/tier-improvements", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addTierImprovement(JwtAuthenticationToken principal, @Valid @RequestBody TierImprovementRequest request) {
        final var userEmail = this.userService.getUserEmailFromPrincipal(principal);
        if (userEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User email not found"));
        }

        final var patreonAccessToken = this.userPatreonAccessTokenRepository.findByUserEmail(userEmail.get());
        if (patreonAccessToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final var accountInfo = this.patreonService.ofUserPatreonAccessToken(patreonAccessToken.get());

        final var campaign = accountInfo.getCampaignWithChildrenTiers();
        final var campaignTiers = campaign.getChildrenTiers();
        final var tierOpt = campaignTiers.stream()
                .filter(tier -> Objects.equals(tier.getId(), request.getTierId()))
                .findFirst();
        if (!(Objects.equals(campaign.getCampaign().getId(), request.getCampaignId())
                && tierOpt.isPresent()
                && Objects.equals(tierOpt.get().getId(), request.getTierId()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final var savedEntity = this.tierImprovementRepository.save(
                this.tierImprovementRepository.findByCampaignIdAndTierId(request.getCampaignId(), request.getTierId())
                        .map(tierImprovement -> tierImprovement.setChatId(request.getChatId()))
                        .orElseGet(() -> TierImprovement.builder()
                                .ownerEmail(userEmail.get())
                                .campaignId(request.getCampaignId())
                                .tierId(request.getTierId())
                                .chatId(request.getChatId())
                                .build()));


        return ResponseEntity.ok(savedEntity);
    }

    @GetMapping(value = "/private/tier-improvements")
    public ResponseEntity<?> tierImprovements(JwtAuthenticationToken principal) {
        final var userEmail = this.userService.getUserEmailFromPrincipal(principal);
        if (userEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User email not found"));
        }

        final var tierImprovemnts = this.tierImprovementRepository.findByOwnerEmail(userEmail.get());

        return ResponseEntity.ok(Map.of("tierImprovements", tierImprovemnts));
    }

    @DeleteMapping(value = "/private/tier-improvements")
    public ResponseEntity<?> deleteTierImprovement(JwtAuthenticationToken principal, @RequestParam Long campaignId, @RequestParam Long tierId) {
        final var userEmail = this.userService.getUserEmailFromPrincipal(principal);
        if (userEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User email not found"));
        }

        this.tierImprovementRepository.findByOwnerEmailAndCampaignIdAndTierId(userEmail.get(), campaignId, tierId)
                .ifPresent(this.tierImprovementRepository::delete);

        return ResponseEntity.ok().build();
    }

}
