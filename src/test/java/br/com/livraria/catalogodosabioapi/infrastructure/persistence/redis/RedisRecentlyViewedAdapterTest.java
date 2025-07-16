package br.com.livraria.catalogodosabioapi.infrastructure.persistence.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisRecentlyViewedAdapterTest {

    @Mock // Mock do RedisTemplate
    private RedisTemplate<String, String> redisTemplate;

    @Mock // Mock do ListOperations, que será retornado pelo redisTemplate.opsForList()
    private ListOperations<String, String> listOps;

    private RedisRecentlyViewedAdapter redisRecentlyViewedAdapter;

    private static final String KEY_PREFIX = "recently_viewed:";
    private static final int MAX_ITEMS = 10;

    @BeforeEach
    void setUp() {
        // Configura o mock do redisTemplate para retornar o mock de listOps
        when(redisTemplate.opsForList()).thenReturn(listOps);
        // Instancia o adaptador, que agora receberá o redisTemplate mockado
        redisRecentlyViewedAdapter = new RedisRecentlyViewedAdapter(redisTemplate);
    }

    @Test
    @DisplayName("Deve salvar um livro na lista de visualizados recentemente")
    void shouldSaveBookInRecentlyViewedList() {
        // Arrange
        String clientId = "user-1";
        String bookId = "book-a";
        String key = KEY_PREFIX + clientId;

        // Act
        redisRecentlyViewedAdapter.save(clientId, bookId);

        // Assert
        verify(listOps, times(1)).remove(key, 0, bookId);
        verify(listOps, times(1)).leftPush(key, bookId);
        verify(listOps, times(1)).trim(key, 0, MAX_ITEMS - 1);
        verify(redisTemplate, times(1)).expire(any(String.class), any(Duration.class));
    }

    @Test
    @DisplayName("Deve retornar livros visualizados recentemente por clientId")
    void shouldReturnRecentlyViewedBooksByClientId() {
        // Arrange
        String clientId = "user-2";
        String key = KEY_PREFIX + clientId;
        List<String> bookIds = Arrays.asList("book-c", "book-b", "book-a"); // Ordem esperada do Redis (mais recente primeiro)

        // Configura o mock para retornar a lista de IDs
        when(listOps.range(key, 0, -1)).thenReturn(bookIds);

        // Act
        List<String> result = redisRecentlyViewedAdapter.findByClientId(clientId);

        // Assert
        // Verifica se o método range foi chamado com a chave correta
        verify(listOps, times(1)).range(key, 0, -1);
        // Verifica se a lista retornada é a esperada
        assertEquals(bookIds, result, "A lista de IDs de livros deve ser a mesma que a retornada pelo Redis.");
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum livro foi visualizado recentemente para o clientId")
    void shouldReturnEmptyListWhenNoRecentlyViewedBooksForClientId() {
        // Arrange
        String clientId = "user-3";
        String key = KEY_PREFIX + clientId;

        // Configura o mock para retornar uma lista vazia
        when(listOps.range(key, 0, -1)).thenReturn(Collections.emptyList());

        // Act
        List<String> result = redisRecentlyViewedAdapter.findByClientId(clientId);

        // Assert
        // Verifica se o método range foi chamado
        verify(listOps, times(1)).range(key, 0, -1);
        // Verifica se a lista retornada está vazia
        assertTrue(result.isEmpty(), "A lista deve ser vazia se o Redis não retornar IDs.");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando listOps.range retorna nulo (comportamento defensivo)")
    void shouldReturnEmptyListWhenListOpsRangeReturnsNull() {
        // Arrange
        String clientId = "user-4";
        String key = KEY_PREFIX + clientId;

        // Configura o mock para retornar nulo (caso improvável, mas para robustez)
        when(listOps.range(key, 0, -1)).thenReturn(null);

        // Act
        List<String> result = redisRecentlyViewedAdapter.findByClientId(clientId);

        // Assert
        verify(listOps, times(1)).range(key, 0, -1);
        assertTrue(result.isEmpty(), "A lista deve ser vazia se listOps.range retornar nulo.");
    }
}
