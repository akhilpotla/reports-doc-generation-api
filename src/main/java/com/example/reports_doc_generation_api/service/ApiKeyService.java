package com.example.reports_doc_generation_api.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import com.example.reports_doc_generation_api.model.ApiEntity;
import com.example.reports_doc_generation_api.model.Status;
import org.springframework.stereotype.Service;

import com.example.reports_doc_generation_api.model.UserEntity;
import com.example.reports_doc_generation_api.repository.ApiRepository;
import com.example.reports_doc_generation_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiKeyService {

    private final UserRepository users;
    private final ApiRepository apis;
    private final SecureRandom rng = new SecureRandom();

    @Value("${app.api-key.length:32}")
    private int keyBytes;

    @Value("${app.api-key.prefix:}")
    private String prefix;

    public ApiKeyService(UserRepository users, ApiRepository apis) {
        this.users = users;
        this.apis = apis;
    }

    @Transactional
    public String generateOrRotateForEmail(String email) {
        UserEntity user = users.findByEmail(email).orElseThrow();

        String rawKey = prefix + base64Url(randomBytes(keyBytes));
        String hashHex = sha256Hex(rawKey);

        apis.findAll().stream()
                .filter(a -> a.getUser().getId().equals(user.getId()) && a.getStatus() == Status.ACTIVE)
                .forEach(a -> {
                    a.setStatus(Status.INACTIVE);
                    apis.save(a);
                });

        ApiEntity api = new ApiEntity();
        api.setApiToken(hashHex);
        api.setStatus(Status.ACTIVE);
        api.setUser(user);
        apis.save(api);

        return rawKey;
    }

    @Transactional(readOnly = true)
    public boolean hasActiveApiKey(String email) {
        UserEntity user = users.findByEmail(email).orElseThrow();
        return apis.findAll().stream()
                .allMatch(a -> a.getUser().getId().equals(user.getId()) && a.getStatus() == Status.ACTIVE);
    }

    @Transactional
    public void deactivateForEmail(String email) {
        UserEntity user = users.findByEmail(email).orElseThrow();
        apis.findAll().stream()
                .filter(a -> a.getUser().getId().equals(user.getId()) && a.getStatus() == Status.ACTIVE)
                .forEach(a -> {
                    a.setStatus(Status.INACTIVE);
                    apis.save(a);
                });
    }

    public String getApiToken(String email) {
        UserEntity user = users.findByEmail(email).orElseThrow();
        ApiEntity api = apis.findAll().stream()
                .filter(a -> a.getUser().getId().equals(user.getId()) && a.getStatus() == Status.ACTIVE)
                .findFirst().orElseThrow();
        return api.getApiToken();
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        rng.nextBytes(bytes);
        return bytes;
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte by : dig)
                sb.append(String.format("%02x", by));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
