package dev.joserg.communify.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "patreon")
@Data
public class PatreonProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
