package com.example.reports_doc_generation_api.security;

import com.example.reports_doc_generation_api.model.Status;
import com.example.reports_doc_generation_api.repository.ApiRepository;
import com.example.reports_doc_generation_api.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final UserRepository users;
    private final ApiRepository apis;

    @Value("${app.api-key.header:X-API-Key}")
    private String headerName;

    public ApiKeyAuthFilter(UserRepository users, ApiRepository apis) {
        this.users = users;
        this.apis = apis;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String raw = request.getHeader(headerName);
        if (raw == null || raw.isEmpty()) {
            raw = request.getHeader("X-API-Key");
        }

        if (raw != null && !raw.isBlank()) {
            String hash = sha256Hex(raw);

            apis.findByApiTokenAndStatus(hash, Status.ACTIVE).ifPresent(api -> {
                var user = api.getUser();
                var auth = new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_API_USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        filterChain.doFilter(request, response);
    }

    private String sha256Hex(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var dig = md.digest(s.getBytes());
            var sb = new StringBuilder();
            for (byte b : dig)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
