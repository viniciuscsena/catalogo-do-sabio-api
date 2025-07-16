package br.com.livraria.catalogodosabioapi.infrastructure.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "google.aistudio")
public record AiStudioProperties(
        @NotBlank String apiKey,
        @NotBlank String apiUrl,
        @NotNull Resource requestSchema
) {}
