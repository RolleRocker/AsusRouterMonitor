package com.asusrouter.infrastructure.config;

import java.time.Duration;

import javax.net.ssl.SSLException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;

/**
 * Configuration for WebClient used to communicate with ASUS Router.
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    
    private final RouterProperties routerProperties;
    
    @Bean
    public WebClient webClient() {
        validateRouterProperties();
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(routerProperties.getReadTimeout()))
            .secure(sslContextSpec -> {
                try {
                    // Trust all certificates (router often uses self-signed cert)
                    SslContext sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
                    sslContextSpec.sslContext(sslContext);
                } catch (SSLException e) {
                    throw new IllegalStateException("Failed to configure SSL for router communication", e);
                }
            });
        
        String baseUrl = java.util.Objects.requireNonNull(
            routerProperties.getBaseUrl(), 
            "Router base URL must not be null"
        );
        
        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(
                java.util.Objects.requireNonNull(httpClient, "HttpClient must not be null")
            ))
            .build();
    }
    
    /**
     * Validate router properties before creating WebClient.
     * Fails fast with clear error messages if configuration is invalid.
     */
    private void validateRouterProperties() {
        if (routerProperties == null) {
            throw new IllegalStateException("RouterProperties must not be null");
        }
        if (routerProperties.getBaseUrl() == null || routerProperties.getBaseUrl().trim().isEmpty()) {
            throw new IllegalStateException("Router base URL must be configured");
        }
        if (routerProperties.getReadTimeout() <= 0) {
            throw new IllegalStateException("Router read timeout must be positive");
        }
    }
}
