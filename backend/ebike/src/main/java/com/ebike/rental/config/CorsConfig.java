package com.ebike.rental.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = resolveAllowedOrigins();
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    private List<String> resolveAllowedOrigins() {
        String configuredOrigins = System.getenv("APP_CORS_ALLOWED_ORIGINS");
        List<String> origins = new ArrayList<>();

        if (configuredOrigins != null && !configuredOrigins.isBlank()) {
            origins.addAll(Arrays.stream(configuredOrigins.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isBlank())
                    .collect(Collectors.toList()));
        }

        if (origins.isEmpty()) {
            origins.addAll(Arrays.asList("http://localhost:8081", "http://localhost:8080", "http://localhost:5173"));
        }

        return origins;
    }
}
