package br.com.livraria.catalogodosabioapi.infrastructure.configuration;

import br.com.livraria.catalogodosabioapi.core.usecase.BookUseCaseImpl;
import br.com.livraria.catalogodosabioapi.core.usecase.RecentlyViewedUseCaseImpl;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.BookUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.RecentlyViewedUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.BookRepositoryPort;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.RecentlyViewedPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

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
}
