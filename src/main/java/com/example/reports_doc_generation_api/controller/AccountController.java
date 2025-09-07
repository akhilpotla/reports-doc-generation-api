package com.example.reports_doc_generation_api.controller;

import com.example.reports_doc_generation_api.model.UserEntity;
import com.example.reports_doc_generation_api.repository.UserRepository;
import com.example.reports_doc_generation_api.service.ApiKeyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final UserRepository users;
    private final ApiKeyService apiKeys;

    public AccountController(UserRepository users, ApiKeyService apiKeys) {
        this.users = users;
        this.apiKeys = apiKeys;
    }

    @GetMapping
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User me) {
        String email = (String) me.getAttributes().get("email");
        UserEntity u = users.findByEmail(email).orElseThrow();
        boolean hasActive = apiKeys.hasActiveApiKey(email);
        return Map.of("email", email, "hasActiveApiKey", hasActive);
    }

    @GetMapping("/api-key/generate")
    public Map<String, Object> generateGET(@AuthenticationPrincipal OidcUser me) {
        String plaintext = apiKeys.getApiToken(me.getEmail());
        return Map.of("apiKey", plaintext);
    }

    @PostMapping("/api-key/generate")
    public Map<String, Object> generate(@AuthenticationPrincipal OAuth2User me) {
        String email = (String) me.getAttributes().get("email");
        String plaintext = apiKeys.generateOrRotateForEmail(email);
        return Map.of("apiKey", plaintext); // display once
    }

    @PostMapping("/api-key/deactivate")
    public void deactivate(@AuthenticationPrincipal OAuth2User me) {
        String email = (String) me.getAttributes().get("email");
        apiKeys.deactivateForEmail(email);
    }
}
