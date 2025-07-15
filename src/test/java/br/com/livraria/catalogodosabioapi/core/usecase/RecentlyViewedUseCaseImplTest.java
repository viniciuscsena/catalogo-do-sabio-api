package br.com.livraria.catalogodosabioapi.core.usecase;

import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.BookUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.RecentlyViewedPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecentlyViewedUseCaseImplTest {

    @Mock
    private RecentlyViewedPort recentlyViewedPort;

    @Mock
    private BookUseCase bookUseCase;

    @InjectMocks
    private RecentlyViewedUseCaseImpl recentlyViewedUseCase;

    @Test
    @DisplayName("Deve salvar o ID do livro quando track é chamado com um clientId válido")
    void shouldSaveBookIdWhenTrackIsCalledWithValidClientId() {
        // Arrange
        String clientId = "client-123";
        String bookId = "book-abc";

        // Act
        recentlyViewedUseCase.track(clientId, bookId);

        // Assert
        verify(recentlyViewedPort, times(1)).save(clientId, bookId);
    }

    @Test
    @DisplayName("Não deve salvar o ID do livro quando track é chamado com clientId nulo")
    void shouldNotSaveBookIdWhenTrackIsCalledWithNullClientId() {
        // Arrange
        String clientId = null;
        String bookId = "book-abc";

        // Act
        recentlyViewedUseCase.track(clientId, bookId);

        // Assert
        verify(recentlyViewedPort, never()).save(anyString(), anyString());
    }

    @Test
    @DisplayName("Não deve salvar o ID do livro quando track é chamado com clientId vazio")
    void shouldNotSaveBookIdWhenTrackIsCalledWithEmptyClientId() {
        // Arrange
        String clientId = "";
        String bookId = "book-abc";

        // Act
        recentlyViewedUseCase.track(clientId, bookId);

        // Assert
        verify(recentlyViewedPort, never()).save(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando find é chamado com clientId nulo")
    void shouldReturnEmptyListWhenFindIsCalledWithNullClientId() {
        // Arrange
        String clientId = null;

        // Act
        List<BookEntity> result = recentlyViewedUseCase.find(clientId);

        // Assert
        assertTrue(result.isEmpty(), "A lista deve ser vazia para clientId nulo.");
        verify(recentlyViewedPort, never()).findByClientId(anyString());
        verify(bookUseCase, never()).findAllByIds(anyList());
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando find é chamado com clientId vazio")
    void shouldReturnEmptyListWhenFindIsCalledWithEmptyClientId() {
        // Arrange
        String clientId = "";

        // Act
        List<BookEntity> result = recentlyViewedUseCase.find(clientId);

        // Assert
        assertTrue(result.isEmpty(), "A lista deve ser vazia para clientId vazio.");
        verify(recentlyViewedPort, never()).findByClientId(anyString());
        verify(bookUseCase, never()).findAllByIds(anyList());
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando findByClientId retorna lista vazia")
    void shouldReturnEmptyListWhenFindByClientIdReturnsEmptyList() {
        // Arrange
        String clientId = "client-456";
        when(recentlyViewedPort.findByClientId(clientId)).thenReturn(Collections.emptyList());

        // Act
        List<BookEntity> result = recentlyViewedUseCase.find(clientId);

        // Assert
        verify(recentlyViewedPort, times(1)).findByClientId(clientId);
        assertTrue(result.isEmpty(), "A lista deve ser vazia se a porta não retornar IDs.");
        verify(bookUseCase, never()).findAllByIds(anyList());
    }

    @Test
    @DisplayName("Deve retornar livros visualizados recentemente quando find é chamado com clientId válido")
    void shouldReturnRecentlyViewedBooksWhenFindIsCalledWithValidClientId() {
        // Arrange
        String clientId = "client-789";
        List<String> bookIds = Arrays.asList("book-id-1", "book-id-2");
        List<BookEntity> expectedBooks = Arrays.asList(
                new BookEntity("book-id-1", "Book One", List.of("Author A"), List.of("Genre X"), "Desc X", 10.0, 5),
                new BookEntity("book-id-2", "Book Two", List.of("Author B"), List.of("Genre Y"), "Desc Y", 20.0, 10)
        );

        when(recentlyViewedPort.findByClientId(clientId)).thenReturn(bookIds);
        when(bookUseCase.findAllByIds(bookIds)).thenReturn(expectedBooks);

        // Act
        List<BookEntity> result = recentlyViewedUseCase.find(clientId);

        // Assert
        verify(recentlyViewedPort, times(1)).findByClientId(clientId);
        verify(bookUseCase, times(1)).findAllByIds(bookIds);
        assertEquals(expectedBooks, result, "A lista de livros retornada deve ser a esperada.");
        assertEquals(2, result.size(), "A lista deve conter 2 livros.");
    }
}
