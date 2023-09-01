package dev.joserg.communify.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PatreonAccessToken {
    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("expires_in")
    Integer expiresIn;
    @JsonProperty("token_type")
    String tokenType;
    String scope;
    @JsonProperty("refresh_token")
    String refreshToken;
    String version;
}

// {access_token=F11nIyyBDuFI3meCL0RdOqSs2m1aDw0nhjEYR_MKRVA, expires_in=2678400, token_type=Bearer, scope=identity identity[email] identity.memberships campaigns w:campaigns.webhook campaigns.members, refresh_token=_zcolQ9HxLauqLBe2VkA0c-0kFKKiFhkEx1bnqcjBKw, version=0.0.1}