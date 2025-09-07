package com.example.reports_doc_generation_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final ApiKeyAuthFilter apiKeyFilter;
    private final OAuth2LoginSuccessHandler successHandler;

    public SecurityConfig(ApiKeyAuthFilter apiKeyFilter, OAuth2LoginSuccessHandler successHandler) {
        this.apiKeyFilter = apiKeyFilter;
        this.successHandler = successHandler;
    }

    @Bean
    @Order(1)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("API_USER"))
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        e -> e.authenticationEntryPoint((req, res, ex) -> res.sendError(401, "Unauthorized")));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain web(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/health", "/assets/**").permitAll()
                .requestMatchers("/account/**").authenticated()
                .anyRequest().permitAll())
                .oauth2Login(o -> o.successHandler(successHandler))
                .logout(l -> l.logoutSuccessUrl("/").permitAll());
        return http.build();
    }
}
