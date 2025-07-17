package br.com.livraria.catalogodosabioapi.infrastructure.configuration;

import br.com.livraria.catalogodosabioapi.core.usecase.BookUseCaseImpl;
import br.com.livraria.catalogodosabioapi.core.usecase.RecentlyViewedUseCaseImpl;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.BookUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.RecentlyViewedUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.BookRepositoryPort;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.RecentlyViewedPort;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class BeanConfiguration {
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }

    @Bean
    public BookUseCase bookUseCase(final BookRepositoryPort bookRepositoryPort){
        return new BookUseCaseImpl(bookRepositoryPort);
    }

    @Bean
    public RecentlyViewedUseCase recentlyViewedUseCase(RecentlyViewedPort recentlyViewedPort, BookUseCase bookUseCase){
        return new RecentlyViewedUseCaseImpl(recentlyViewedPort, bookUseCase);
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(CachingProperties cachingProperties) {
        return (builder) -> {
            RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

            builder.cacheDefaults(defaultCacheConfig);

            cachingProperties.ttls().forEach((cacheName, ttl) ->
                    builder.withCacheConfiguration(cacheName, defaultCacheConfig.entryTtl(ttl))
            );
        };
    }
}
