package dev.joserg.communify.controller;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RootController {

  @GetMapping("/public")
  public String publicResource() {
    return "This is a public resource";
  }

  @GetMapping("/private")
  @PreAuthorize("hasAuthority('SCOPE_Root.Private')")
  public Map<String, Object> privateResource(JwtAuthenticationToken jwtAuthenticationToken) {
    return jwtAuthenticationToken.getTokenAttributes();
  }
}
