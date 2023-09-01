package dev.joserg.communify.events;

import dev.joserg.communify.configuration.TelegramProperties;
import dev.joserg.communify.repository.ImprovementClaimRepository;
import dev.joserg.communify.repository.TierImprovementRepository;
import dev.joserg.communify.telegram.CommunifyBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotStarter implements ApplicationListener<ApplicationReadyEvent> {
    private final ImprovementClaimRepository improvementClaimRepository;
    private final TierImprovementRepository tierImprovementRepository;
    private final TelegramProperties telegramProperties;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            final var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new CommunifyBot(telegramProperties.getToken(), improvementClaimRepository, tierImprovementRepository));
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
    }
}
