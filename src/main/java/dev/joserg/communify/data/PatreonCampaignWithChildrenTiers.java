package dev.joserg.communify.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PatreonCampaignWithChildrenTiers {
    private PatreonCampaign campaign;
    private List<PatreonTier> childrenTiers;
}
