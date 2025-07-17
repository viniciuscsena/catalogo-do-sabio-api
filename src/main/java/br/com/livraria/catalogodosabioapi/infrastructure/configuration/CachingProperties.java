package br.com.livraria.catalogodosabioapi.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties(prefix = "caching")
public record CachingProperties( Map<String, Duration> ttls) {
}
