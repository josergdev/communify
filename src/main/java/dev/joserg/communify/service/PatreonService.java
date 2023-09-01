package dev.joserg.communify.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.joserg.communify.configuration.PatreonProperties;
import dev.joserg.communify.data.*;
import dev.joserg.communify.entity.PatreonAccessToken;
import dev.joserg.communify.entity.UserPatreonAccessToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class PatreonService {

    private final PatreonProperties patreonProperties;

    public Optional<PatreonAccessToken> acquirePatreonAccessToken(String authorizationCode) {
        final var token = WebClient.create()
                .post()
                .uri("https://www.patreon.com/api/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("code", authorizationCode)
                        .with("grant_type", "authorization_code")
                        .with("client_id", patreonProperties.getClientId())
                        .with("client_secret", patreonProperties.getClientSecret())
                        .with("redirect_uri", patreonProperties.getRedirectUri())
                )
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(PatreonAccessToken.class))
                .block();
        if (Objects.isNull(token.getAccessToken()) || token.getAccessToken().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(token);
    }

    public PatreonAccountInfo ofUserPatreonAccessToken(UserPatreonAccessToken patreonAccessToken) {
        final var identityData = WebClient.create()
                .get()
                .uri("https://www.patreon.com/api/oauth2/v2/identity?include=memberships.campaign,memberships.currently_entitled_tiers,campaign.tiers&fields[campaign]=creation_name&fields[user]=full_name,email&fields[tier]=title")
                .header("Authorization", "Bearer " + patreonAccessToken.getPatreonAccessToken().getAccessToken())
                .retrieve()
                .bodyToMono(PatreonApiResponse.Identity.class)
                .block();

        final var included = identityData.getIncluded() == null ? List.<ObjectNode>of() : identityData.getIncluded();
        final var membersById = included.stream()
                .filter(node -> node.get("type").asText().equals("member"))
                .collect(Collectors.toMap(node -> node.get("id").asText(), Function.identity()));
        final var campaignsById = included.stream()
                .filter(node -> node.get("type").asText().equals("campaign"))
                .collect(Collectors.toMap(node -> node.get("id").asText(), Function.identity()));
        final var tiersById = included.stream()
                .filter(node -> node.get("type").asText().equals("tier"))
                .collect(Collectors.toMap(node -> node.get("id").asText(), Function.identity()));

        final var accountInfo = PatreonAccountInfo.builder()
                .fullName(identityData.getData().getAttributes().getFull_name())
                .email(identityData.getData().getAttributes().getEmail())
                .campaignWithChildrenTiers(this.getCampaignWithChildrenTiers(identityData, campaignsById, tiersById))
                .currentlyEntitledTiers(this.getPatreonTierWithParentCampaigns(identityData, membersById, campaignsById, tiersById))
                .build();
        return accountInfo;
    }

    private List<PatreonTierWithParentCampaign> getPatreonTierWithParentCampaigns(PatreonApiResponse.Identity identityData, Map<String, ObjectNode> membersById, Map<String, ObjectNode> campaignsById, Map<String, ObjectNode> tiersById) {
        final var membershipsIds = identityData.getData() == null ? List.<String>of() : identityData.getData().getRelationships().getMemberships().getData().stream().map(PatreonApiResponse.MembershipData::getId).toList();
        return membersById.values().stream()
                .filter(nm -> membershipsIds.contains(nm.get("id").asText()))
                .flatMap(node -> {
                    final var campaignId = node.get("relationships").get("campaign").get("data").get("id");
                    final var entitledTiersData = node.get("relationships").get("currently_entitled_tiers").get("data");
                    return StreamSupport.stream(entitledTiersData.spliterator(), false)
                            .map(nt -> PatreonTierWithParentCampaign.builder()
                                    .tier(PatreonTier.builder()
                                            .id(nt.get("id").asLong())
                                            .name(tiersById.get(nt.get("id").asText()).get("attributes").get("title").asText())
                                            .build())
                                    .parentCampaign(PatreonCampaign.builder()
                                            .id(campaignId.asLong())
                                            .name(campaignsById.get(campaignId.asText()).get("attributes").get("creation_name").asText())
                                            .build())
                                    .build());
                }).toList();
    }

    private PatreonCampaignWithChildrenTiers getCampaignWithChildrenTiers(PatreonApiResponse.Identity identityData, Map<String, ObjectNode> campaignsById, Map<String, ObjectNode> tiersById) {
        final var campaignData = identityData.getData().getRelationships().getCampaign().getData();
        if (campaignData == null || campaignData.getId() == null) {
            return null;
        }
        final var campaignId = campaignData.getId();
        final var campaignsTiersData = campaignsById.get(campaignId).get("relationships").get("tiers").get("data");
        final var tiers = StreamSupport.stream(campaignsTiersData.spliterator(), false)
                .map(nt -> PatreonTier.builder()
                        .id(nt.get("id").asLong())
                        .name(tiersById.get(nt.get("id").asText()).get("attributes").get("title").asText())
                        .build())
                .toList();
        final var campaign = PatreonCampaign.builder()
                .id(Long.valueOf(campaignId))
                .name(campaignsById.get(campaignId).get("attributes").get("creation_name").asText())
                .build();
        return PatreonCampaignWithChildrenTiers.builder()
                .campaign(campaign)
                .childrenTiers(tiers)
                .build();
    }
}
