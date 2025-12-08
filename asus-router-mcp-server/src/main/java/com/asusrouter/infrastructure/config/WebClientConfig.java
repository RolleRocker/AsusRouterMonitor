package com.asusrouter.infrastructure.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.Duration;

/**
 * Configuration for WebClient used to communicate with ASUS Router.
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    
    private final RouterProperties routerProperties;
    
    @Bean
    public WebClient webClient() throws SSLException {
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
                    throw new RuntimeException("Failed to configure SSL", e);
                }
            });
        
        return WebClient.builder()
            .baseUrl(routerProperties.getBaseUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
