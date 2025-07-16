package br.com.livraria.catalogodosabioapi.infrastructure.web.controller;

import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;
import br.com.livraria.catalogodosabioapi.core.domain.exception.BookNotFoundException;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.BookUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.RecentlyViewedUseCase;
import br.com.livraria.catalogodosabioapi.infrastructure.web.mapper.BookApiMapper;
import br.com.livraria.catalogodosabioapi.infrastructure.web.mapper.BookApiMapperImpl; // Importa a implementação real do mapper
import br.com.livraria.catalogodosabioapi.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookUseCase bookUseCase;
    @Mock
    private RecentlyViewedUseCase recentlyViewedUseCase;

    private BookApiMapper bookApiMapper = new BookApiMapperImpl();;

    private BookController bookController;

    // Entidades de domínio para uso nos testes
    private BookEntity mockBookEntity;
    private List<BookEntity> mockBookEntityList;

    @BeforeEach
    void setUp() {

        mockBookEntity = new BookEntity("1", "Test Book", List.of("Author A"), List.of("Genre X"), "Desc", 10.0, 5);
        mockBookEntityList = Arrays.asList(
                mockBookEntity,
                new BookEntity("2", "Another Book", List.of("Author B"), List.of("Genre Y"), "Desc2", 20.0, 10)
        );

        bookController = new BookController(bookUseCase, recentlyViewedUseCase, bookApiMapper);
    }

    @Test
    @DisplayName("booksIdGet: Deve retornar 200 OK e um livro quando o ID existe e rastrear visualização")
    void booksIdGet_shouldReturnOkAndBookAndTrackView() {
        // Arrange
        String bookId = "1";
        String clientId = UUID.randomUUID().toString();

        when(bookUseCase.findById(bookId)).thenReturn(mockBookEntity);
        Book expectedBookApiModel = bookApiMapper.toApi(mockBookEntity);

        // Act
        ResponseEntity<Book> response = bookController.booksIdGet(bookId, clientId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBookApiModel, response.getBody());

        verify(bookUseCase, times(1)).findById(bookId);
        verify(recentlyViewedUseCase, times(1)).track(clientId, bookId);
    }

    @Test
    @DisplayName("booksIdGet: Deve retornar 200 OK e um livro e não rastrear visualização se xClientID for nulo")
    void booksIdGet_shouldReturnOkAndBookAndNotTrackViewIfClientIdIsNull() {
        // Arrange
        String bookId = "1";
        String clientId = null; // Cliente ID nulo

        when(bookUseCase.findById(bookId)).thenReturn(mockBookEntity);
        Book expectedBookApiModel = bookApiMapper.toApi(mockBookEntity);

        // Act
        ResponseEntity<Book> response = bookController.booksIdGet(bookId, clientId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBookApiModel, response.getBody());

        verify(bookUseCase, times(1)).findById(bookId);
        verify(recentlyViewedUseCase, never()).track(anyString(), anyString());
    }

    @Test
    @DisplayName("booksIdGet: Deve lançar BookNotFoundException quando o ID não existe")
    void booksIdGet_shouldThrowBookNotFoundExceptionWhenIdDoesNotExist() {
        // Arrange
        String bookId = "non-existent";
        String clientId = UUID.randomUUID().toString();

        when(bookUseCase.findById(bookId)).thenThrow(new BookNotFoundException(bookId));

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> bookController.booksIdGet(bookId, clientId));

        verify(bookUseCase, times(1)).findById(bookId);
        verify(recentlyViewedUseCase, never()).track(anyString(), anyString());
    }

    @Test
    @DisplayName("booksGet: Deve retornar 200 OK e uma lista de todos os livros")
    void booksGet_shouldReturnOkAndAllBooks() {
        // Arrange
        when(bookUseCase.findAll()).thenReturn(mockBookEntityList);
        List<Book> expectedBookApiModelList = bookApiMapper.toApi(mockBookEntityList);

        // Act
        ResponseEntity<List<Book>> response = bookController.booksGet();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBookApiModelList, response.getBody());
        assertEquals(2, response.getBody().size());

        verify(bookUseCase, times(1)).findAll();
    }

    @Test
    @DisplayName("booksGet: Deve retornar lista vazia se nenhum livro for encontrado")
    void booksGet_shouldReturnEmptyListWhenNoBooksFound() {
        // Arrange
        when(bookUseCase.findAll()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Book>> response = bookController.booksGet();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(bookUseCase, times(1)).findAll();
    }

    @Test
    @DisplayName("booksAuthorAuthorGet: Deve retornar 200 OK e livros por autor")
    void booksAuthorAuthorGet_shouldReturnOkAndBooksByAuthor() {
        // Arrange
        String author = "Author A";
        when(bookUseCase.findByAuthor(author)).thenReturn(List.of(mockBookEntity));
        List<Book> expectedBookApiModelList = bookApiMapper.toApi(List.of(mockBookEntity));

        // Act
        ResponseEntity<List<Book>> response = bookController.booksAuthorAuthorGet(author);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(expectedBookApiModelList.get(0), response.getBody().get(0));

        verify(bookUseCase, times(1)).findByAuthor(author);
    }

    @Test
    @DisplayName("booksAuthorAuthorGet: Deve retornar lista vazia se nenhum livro for encontrado por autor")
    void booksAuthorAuthorGet_shouldReturnEmptyListWhenNoBooksFoundByAuthor() {
        // Arrange
        String author = "NonExistentAuthor";
        when(bookUseCase.findByAuthor(author)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Book>> response = bookController.booksAuthorAuthorGet(author);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(bookUseCase, times(1)).findByAuthor(author);
    }

    @Test
    @DisplayName("booksGenreGenreGet: Deve retornar 200 OK e livros por gênero")
    void booksGenreGenreGet_shouldReturnOkAndBooksByGenre() {
        // Arrange
        String genre = "Genre X";
        when(bookUseCase.findByGenre(genre)).thenReturn(List.of(mockBookEntity));
        List<Book> expectedBookApiModelList = bookApiMapper.toApi(List.of(mockBookEntity));

        // Act
        ResponseEntity<List<Book>> response = bookController.booksGenreGenreGet(genre);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(expectedBookApiModelList.get(0), response.getBody().get(0));

        verify(bookUseCase, times(1)).findByGenre(genre);
    }

    @Test
    @DisplayName("booksGenreGenreGet: Deve retornar lista vazia se nenhum livro for encontrado por gênero")
    void booksGenreGenreGet_shouldReturnEmptyListWhenNoBooksFoundByGenre() {
        // Arrange
        String genre = "NonExistentGenre";
        when(bookUseCase.findByGenre(genre)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Book>> response = bookController.booksGenreGenreGet(genre);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(bookUseCase, times(1)).findByGenre(genre);
    }

    @Test
    @DisplayName("booksRecentlyViewedGet: Deve retornar 200 OK e livros visualizados recentemente")
    void booksRecentlyViewedGet_shouldReturnOkAndRecentlyViewedBooks() {
        // Arrange
        String clientId = UUID.randomUUID().toString();
        when(recentlyViewedUseCase.find(clientId)).thenReturn(mockBookEntityList);
        List<Book> expectedBookApiModelList = bookApiMapper.toApi(mockBookEntityList);

        // Act
        ResponseEntity<List<Book>> response = bookController.booksRecentlyViewedGet(clientId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBookApiModelList, response.getBody());
        assertEquals(2, response.getBody().size());

        verify(recentlyViewedUseCase, times(1)).find(clientId);
    }

    @Test
    @DisplayName("booksRecentlyViewedGet: Deve retornar lista vazia se nenhum livro visualizado recentemente")
    void booksRecentlyViewedGet_shouldReturnEmptyListWhenNoRecentlyViewedBooks() {
        // Arrange
        String clientId = UUID.randomUUID().toString();
        when(recentlyViewedUseCase.find(clientId)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Book>> response = bookController.booksRecentlyViewedGet(clientId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(recentlyViewedUseCase, times(1)).find(clientId);
    }

    @Test
    @DisplayName("booksRecentlyViewedGet: Deve retornar lista vazia se xClientID for nulo")
    void booksRecentlyViewedGet_shouldReturnEmptyListIfClientIdIsNull() {
        // Arrange
        String clientId = null;

        // Act
        ResponseEntity<List<Book>> response = bookController.booksRecentlyViewedGet(clientId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(recentlyViewedUseCase, never()).find(anyString());
    }
}
