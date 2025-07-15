package br.com.livraria.catalogodosabioapi.infrastructure.web.controller;

import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.BookRepositoryPort;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.document.BookDocument;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.repository.SpringDataBookMongoRepository;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataBookMongoRepository bookMongoRepository;

    @SpyBean
    private BookRepositoryPort bookRepositoryPort;

    static final MongoDBContainer mongoDbContainer = new MongoDBContainer("mongo:7.0");

    static final RedisContainer redisContainer = new RedisContainer("redis:7.2-alpine");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        mongoDbContainer.start();
        redisContainer.start();
        registry.add("spring.data.mongodb.uri", mongoDbContainer::getReplicaSetUrl);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @BeforeEach
    void setUp() {
        bookMongoRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar 200 OK e um livro quando o ID existe")
    void shouldReturnOkAndBookWhenIdExists() throws Exception {
        // Arrange
        BookDocument book = new BookDocument();
        book.setId("123");
        book.setTitle("Duna");
        book.setAuthors(List.of("Frank Herbert"));
        book.setGenres(List.of("Ficção Científica"));

        bookMongoRepository.save(book);

        // Act & Assert
        mockMvc.perform(get("/v1/books/{id}", "123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("123")))
                .andExpect(jsonPath("$.title", is("Duna")));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found quando o ID não existe")
    void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/books/{id}", "id-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("Deve retornar 200 OK e uma lista de livros por género")
    void shouldReturnOkAndBookListWhenSearchingByGenre() throws Exception {
        // Arrange
        BookDocument book1 = new BookDocument();
        book1.setTitle("Livro de Fantasia 1");
        book1.setGenres(List.of("Fantasia", "Aventura"));
        bookMongoRepository.save(book1);

        BookDocument book2 = new BookDocument();
        book2.setTitle("Livro de Fantasia 2");
        book2.setGenres(List.of("Fantasia"));
        bookMongoRepository.save(book2);

        // Act & Assert
        mockMvc.perform(get("/v1/books/genre/{genre}", "Fantasia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Livro de Fantasia 1")));
    }

    @Test
    @DisplayName("Deve servir a partir do cache na segunda chamada para o mesmo ID")
    void shouldServeFromCacheOnSecondCallForSameId() throws Exception {
        // Arrange
        BookDocument book = new BookDocument();
        book.setId("456");
        book.setTitle("Neuromancer");
        book.setAuthors(List.of("William Gibson"));
        book.setGenres(List.of("Cyberpunk"));
        bookMongoRepository.save(book);

        // Act
        mockMvc.perform(get("/v1/books/{id}", "456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Neuromancer")));

        mockMvc.perform(get("/v1/books/{id}", "456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Neuromancer")));

        // Assert
        verify(bookRepositoryPort, times(1)).findById("456");
    }

}
