package dev.joserg.communify.telegram;

import dev.joserg.communify.entity.ImprovementClaim;
import dev.joserg.communify.entity.TierImprovement;
import dev.joserg.communify.repository.ImprovementClaimRepository;
import dev.joserg.communify.repository.TierImprovementRepository;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.groupadministration.DeclineChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Objects;

@Slf4j
public class CommunifyBot extends TelegramLongPollingBot {
    private final ImprovementClaimRepository improvementClaimRepository;
    private final TierImprovementRepository tierImprovementRepository;

    public CommunifyBot(String token, ImprovementClaimRepository improvementClaimRepository, TierImprovementRepository tierImprovementRepository) {
        super(token);
        this.improvementClaimRepository = improvementClaimRepository;
        this.tierImprovementRepository = tierImprovementRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("onUpdateReceived {}", update);
        if (update.hasChatJoinRequest()) {
            final var chatJoinRequest = update.getChatJoinRequest();
            final var userId = chatJoinRequest.getUserChatId();
            final var userName = chatJoinRequest.getUser().getUserName();
            final var chatId = chatJoinRequest.getChat().getId();
            log.info("User with userId {} and username {} wants to join to chat with chatId {}", userId, userName, chatId);
            final var improvements = this.tierImprovementRepository.findByChatId(chatId.toString());
            final var claims = this.improvementClaimRepository.findByTelegramUsername(userName);
            improvements.forEach(improvement -> log.info("TierImprovement: {}", improvement));
            claims.forEach(claim -> log.info("ImprovementClaim: {}", claim));
            if (this.userCanBeAcceptedToChatGroup(userName, chatId, improvements, claims)) {
                approve(userId, chatId);
            } else {
                decline(userId, chatId);
            }
        }
    }

    private Boolean userCanBeAcceptedToChatGroup(String userName, Long chatId, List<TierImprovement> improvements, List<ImprovementClaim> claims) {
        return claims.stream()
                .filter(claim -> Objects.equals(claim.getTelegramUsername(), userName))
                .flatMap(claim -> improvements.stream()
                        .filter(improvement -> Objects.equals(improvement.getChatId(), chatId.toString()))
                        .map(improvement -> new ClaimAndImprovement(claim, improvement)))
                .anyMatch(claimAndImprovement ->
                        Objects.equals(claimAndImprovement.improvementClaim().getCampaignId(), claimAndImprovement.tierImprovement().getCampaignId())
                                && Objects.equals(claimAndImprovement.improvementClaim().getTierId(), claimAndImprovement.tierImprovement().getTierId()));
    }

    private void approve(Long userId, Long chatId) {
        try {
            this.execute(new ApproveChatJoinRequest(chatId.toString(), userId));
            log.info("Approved user with userId {} on chatId {}", userId, chatId);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void decline(Long userId, Long chatId) {
        try {
            this.execute(new DeclineChatJoinRequest(chatId.toString(), userId));
            log.info("Declined user with userId {} on chatId {}", userId, chatId);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return "communifydev_bot";
    }

    private record ClaimAndImprovement(ImprovementClaim improvementClaim,
                                       TierImprovement tierImprovement) {
    }
}
