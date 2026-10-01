package io.github.jenboc.smite_build_api.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.PropertyNamingStrategies;

/**
 * SpringBoot Configuration for Jackson. Accounts for the fact that
 * the .json files use snake_case, rather than camelCase, and that
 * our Java enums use CAPITALISED_SNAKE_CASE
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomiser() {
        return builder -> builder
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }
}
