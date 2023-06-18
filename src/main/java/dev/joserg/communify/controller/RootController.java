package dev.joserg.communify.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RootController {

    private final BuildProperties buildProperties;

    @GetMapping("/public")
    public String publicResource() {
        return buildProperties.getVersion();
    }

    @GetMapping("/private")
    @PreAuthorize("hasAuthority('SCOPE_Root.Private')")
    public Map<String, Object> privateResource(JwtAuthenticationToken jwtAuthenticationToken) {
        return jwtAuthenticationToken.getTokenAttributes();
    }
}
