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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
