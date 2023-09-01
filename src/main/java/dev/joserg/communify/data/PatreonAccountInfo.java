package dev.joserg.communify.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PatreonAccountInfo {
    private String email;
    private String fullName;
    private PatreonCampaignWithChildrenTiers campaignWithChildrenTiers;
    private List<PatreonTierWithParentCampaign> currentlyEntitledTiers;
}
