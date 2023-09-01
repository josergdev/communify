package dev.joserg.communify.repository;

import dev.joserg.communify.entity.ImprovementClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImprovementClaimRepository extends JpaRepository<ImprovementClaim, Long> {

    Optional<ImprovementClaim> findByClaimerEmailAndCampaignIdAndTierId(String ownerEmail, Long campaignId, Long tierId);

    List<ImprovementClaim> findByClaimerEmail(String claimerEmail);

    List<ImprovementClaim> findByTelegramUsername(String telegramUserName);

}
