package dev.joserg.communify.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    public Optional<String> getUserEmailFromPrincipal(JwtAuthenticationToken principal) {
        final var claims = principal.getToken().getClaims();
        if (claims.containsKey("emails")) {
            return ((List<String>) claims.get("emails")).stream().findFirst();
        }
        return Optional.empty();
    }
    
}
