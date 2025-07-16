package br.com.livraria.catalogodosabioapi.infrastructure.configuration;


import br.com.livraria.catalogodosabioapi.core.usecase.BookUseCaseImpl;
import br.com.livraria.catalogodosabioapi.core.usecase.RecentlyViewedUseCaseImpl;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.BookUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.RecentlyViewedUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.BookRepositoryPort;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.RecentlyViewedPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeanConfigurationTest {

    @Mock
    private BookRepositoryPort bookRepositoryPort;
    @Mock
    private RecentlyViewedPort recentlyViewedPort;
    @Mock
    private BookUseCase bookUseCase;

    private BeanConfiguration beanConfiguration;

    @BeforeEach
    void setUp() {
        beanConfiguration = new BeanConfiguration();
    }

    @Test
    @DisplayName("Deve criar e retornar um bean WebClient")
    void shouldCreateWebClientBean() {
        // Act
        WebClient webClient = beanConfiguration.webClient();

        // Assert
        assertNotNull(webClient, "O bean WebClient não deve ser nulo.");
    }

    @Test
    @DisplayName("Deve criar e retornar um bean BookUseCaseImpl com BookRepositoryPort injetado")
    void shouldCreateBookUseCaseBean() {
        // Act
        BookUseCase bookUseCaseBean = beanConfiguration.bookUseCase(bookRepositoryPort);

        // Assert
        assertNotNull(bookUseCaseBean, "O bean BookUseCase não deve ser nulo.");
        assertTrue(bookUseCaseBean instanceof BookUseCaseImpl, "O bean BookUseCase deve ser uma instância de BookUseCaseImpl.");
    }

    @Test
    @DisplayName("Deve criar e retornar um bean RecentlyViewedUseCaseImpl com RecentlyViewedPort e BookUseCase injetados")
    void shouldCreateRecentlyViewedUseCaseBean() {
        // Act
        RecentlyViewedUseCase recentlyViewedUseCaseBean = beanConfiguration.recentlyViewedUseCase(recentlyViewedPort, bookUseCase);

        // Assert
        assertNotNull(recentlyViewedUseCaseBean, "O bean RecentlyViewedUseCase não deve ser nulo.");
        assertTrue(recentlyViewedUseCaseBean instanceof RecentlyViewedUseCaseImpl, "O bean RecentlyViewedUseCase deve ser uma instância de RecentlyViewedUseCaseImpl.");
    }

    @Test
    @DisplayName("Deve customizar o RedisCacheManagerBuilder com os TTLs corretos para cada cache")
    void shouldCustomizeCacheManagerWithCorrectTTLs() {
        // Arrange
        RedisCacheManager.RedisCacheManagerBuilder builder = mock(RedisCacheManager.RedisCacheManagerBuilder.class);
        // Configura o mock para retornar ele mesmo ao chamar withCacheConfiguration, permitindo chamadas encadeadas.
        when(builder.withCacheConfiguration(anyString(), any(RedisCacheConfiguration.class))).thenReturn(builder);

        ArgumentCaptor<String> cacheNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RedisCacheConfiguration> cacheConfigurationCaptor = ArgumentCaptor.forClass(RedisCacheConfiguration.class);

        // Act
        RedisCacheManagerBuilderCustomizer customizer = beanConfiguration.redisCacheManagerBuilderCustomizer();
        customizer.customize(builder);

        // Assert
        verify(builder, times(5)).withCacheConfiguration(cacheNameCaptor.capture(), cacheConfigurationCaptor.capture());

        // Mapeia os nomes dos caches capturados para as suas configurações
        Map<String, RedisCacheConfiguration> capturedConfigurations = new HashMap<>();
        List<String> cacheNames = cacheNameCaptor.getAllValues();
        List<RedisCacheConfiguration> cacheConfigs = cacheConfigurationCaptor.getAllValues();
        for (int i = 0; i < cacheNames.size(); i++) {
            capturedConfigurations.put(cacheNames.get(i), cacheConfigs.get(i));
        }

        // Verifica o TTL para cada cache específico
        assertEquals(Duration.ofHours(1), capturedConfigurations.get("book").getTtl());
        assertEquals(Duration.ofHours(1), capturedConfigurations.get("booksByIds").getTtl());
        assertEquals(Duration.ofMinutes(10), capturedConfigurations.get("books").getTtl());
        assertEquals(Duration.ofMinutes(10), capturedConfigurations.get("booksByGenre").getTtl());
        assertEquals(Duration.ofMinutes(10), capturedConfigurations.get("booksByAuthor").getTtl());
    }
}
