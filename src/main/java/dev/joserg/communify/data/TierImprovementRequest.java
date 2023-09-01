package dev.joserg.communify.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TierImprovementRequest {
    @NotNull
    Long campaignId;
    @NotNull
    Long tierId;
    @NotNull
    @NotBlank
    String chatId;
}
