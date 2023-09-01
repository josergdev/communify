package dev.joserg.communify.repository;

import dev.joserg.communify.entity.TierImprovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TierImprovementRepository extends JpaRepository<TierImprovement, Long> {

    Optional<TierImprovement> findByCampaignIdAndTierId(Long campaignId, Long tierId);

    Optional<TierImprovement> findByOwnerEmailAndCampaignIdAndTierId(String ownerEmail, Long campaignId, Long tierId);

    List<TierImprovement> findByOwnerEmail(String ownerEmail);

    List<TierImprovement> findByChatId(String chatId);
}
