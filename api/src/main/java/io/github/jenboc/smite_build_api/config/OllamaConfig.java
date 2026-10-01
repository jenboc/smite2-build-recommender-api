package io.github.jenboc.smite_build_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * SpringBoot configuration for the RestClient used to access Ollama.
 * Retrieves the base url from the application settings.
 */
@Configuration
public class OllamaConfig {

    @Bean
    public RestClient ollamaRestClient(@Value("${ollama.base-url}") String baseUrl) {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .build();
    }
}
