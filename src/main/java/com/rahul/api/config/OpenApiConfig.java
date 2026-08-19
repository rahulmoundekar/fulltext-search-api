package com.rahul.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productSearchOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Full Text Search API")
                        .version("1.0.0")
                        .description("""
                                Product search API demonstrating
                                PostgreSQL Full-Text Search,
                                relevance ranking,
                                fuzzy search and filtering.
                                """));
    }
}