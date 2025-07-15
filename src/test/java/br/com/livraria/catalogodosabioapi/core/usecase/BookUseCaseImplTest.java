package br.com.livraria.catalogodosabioapi.core.usecase;


import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;
import br.com.livraria.catalogodosabioapi.core.domain.exception.BookNotFoundException;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.BookRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookUseCaseImplTest {

    @Mock
    private BookRepositoryPort bookRepositoryPort;

    @InjectMocks
    private BookUseCaseImpl bookUseCase;

    @Test
    @DisplayName("Deve retornar todos os livros quando findAll é chamado")
    void shouldReturnAllBooksWhenFindAllIsCalled() {
        // Arrange
        List<BookEntity> expectedBooks = Arrays.asList(
                new BookEntity("1", "Title 1", List.of("Author 1"), List.of("Genre 1"), "Desc 1", 10.0, 5),
                new BookEntity("2", "Title 2", List.of("Author 2"), List.of("Genre 2"), "Desc 2", 20.0, 10)
        );
        when(bookRepositoryPort.findAll()).thenReturn(expectedBooks);

        // Act
        List<BookEntity> actualBooks = bookUseCase.findAll();

        // Assert
        verify(bookRepositoryPort, times(1)).findAll();
        assertEquals(expectedBooks, actualBooks, "A lista de livros deve ser a mesma que a retornada pelo repositório.");
        assertEquals(2, actualBooks.size(), "A lista deve conter 2 livros.");
    }

    @Test
    @DisplayName("Deve retornar um livro quando findById é chamado com um ID existente")
    void shouldReturnBookWhenFindByIdIsCalledWithExistingId() {
        // Arrange
        String bookId = "book-123";
        BookEntity expectedBook = new BookEntity(bookId, "Specific Book", List.of("Author A"), List.of("Genre X"), "Desc X", 15.0, 3);
        when(bookRepositoryPort.findById(bookId)).thenReturn(Optional.of(expectedBook));

        // Act
        BookEntity actualBook = bookUseCase.findById(bookId);

        // Assert
        verify(bookRepositoryPort, times(1)).findById(bookId);
        assertEquals(expectedBook, actualBook, "O livro retornado deve ser o livro específico esperado.");
    }

    @Test
    @DisplayName("Deve lançar BookNotFoundException quando findById é chamado com um ID inexistente")
    void shouldThrowBookNotFoundExceptionWhenFindByIdIsCalledWithNonExistingId() {
        // Arrange
        String nonExistingBookId = "non-existent-id";
        // Configura o mock para retornar um Optional vazio quando findById for chamado com o ID inexistente
        when(bookRepositoryPort.findById(nonExistingBookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> bookUseCase.findById(nonExistingBookId),
                "Deve lançar BookNotFoundException para um ID inexistente.");
        verify(bookRepositoryPort, times(1)).findById(nonExistingBookId);
    }

    @Test
    @DisplayName("Deve retornar livros por gênero quando findByGenre é chamado")
    void shouldReturnBooksByGenreWhenFindByGenreIsCalled() {
        // Arrange
        String genre = "Fiction";
        List<BookEntity> expectedBooks = Arrays.asList(
                new BookEntity("3", "Sci-Fi Book", List.of("Author C"), List.of("Fiction"), "Desc C", 25.0, 7),
                new BookEntity("4", "Fantasy Book", List.of("Author D"), List.of("Fiction", "Fantasy"), "Desc D", 30.0, 2)
        );
        when(bookRepositoryPort.findByGenre(genre)).thenReturn(expectedBooks);

        // Act
        List<BookEntity> actualBooks = bookUseCase.findByGenre(genre);

        // Assert
        verify(bookRepositoryPort, times(1)).findByGenre(genre);
        assertEquals(expectedBooks, actualBooks, "A lista de livros por gênero deve ser a mesma que a retornada pelo repositório.");
        assertEquals(2, actualBooks.size(), "A lista deve conter 2 livros.");
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando findByGenre não encontra livros")
    void shouldReturnEmptyListWhenFindByGenreFindsNoBooks() {
        // Arrange
        String genre = "NonExistentGenre";
        when(bookRepositoryPort.findByGenre(genre)).thenReturn(Collections.emptyList());

        // Act
        List<BookEntity> actualBooks = bookUseCase.findByGenre(genre);

        // Assert
        verify(bookRepositoryPort, times(1)).findByGenre(genre);
        assertTrue(actualBooks.isEmpty(), "A lista de livros por gênero deve ser vazia.");
    }

    @Test
    @DisplayName("Deve retornar livros por autor quando findByAuthor é chamado")
    void shouldReturnBooksByAuthorWhenFindByAuthorIsCalled() {
        // Arrange
        String author = "Jane Doe";
        List<BookEntity> expectedBooks = Arrays.asList(
                new BookEntity("5", "Jane's Book 1", List.of("Jane Doe"), List.of("Drama"), "Desc E", 18.0, 6)
        );
        when(bookRepositoryPort.findByAuthor(author)).thenReturn(expectedBooks);

        // Act
        List<BookEntity> actualBooks = bookUseCase.findByAuthor(author);

        // Assert
        verify(bookRepositoryPort, times(1)).findByAuthor(author);
        assertEquals(expectedBooks, actualBooks, "A lista de livros por autor deve ser a mesma que a retornada pelo repositório.");
        assertEquals(1, actualBooks.size(), "A lista deve conter 1 livro.");
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando findByAuthor não encontra livros")
    void shouldReturnEmptyListWhenFindByAuthorFindsNoBooks() {
        // Arrange
        String author = "NonExistentAuthor";
        when(bookRepositoryPort.findByAuthor(author)).thenReturn(Collections.emptyList());

        // Act
        List<BookEntity> actualBooks = bookUseCase.findByAuthor(author);

        // Assert
        verify(bookRepositoryPort, times(1)).findByAuthor(author);
        assertTrue(actualBooks.isEmpty(), "A lista de livros por autor deve ser vazia.");
    }

    @Test
    @DisplayName("Deve retornar livros por lista de IDs quando findAllByIds é chamado")
    void shouldReturnBooksByIdsWhenFindAllByIdsIsCalled() {
        // Arrange
        List<String> bookIds = Arrays.asList("id1", "id2");
        List<BookEntity> expectedBooks = Arrays.asList(
                new BookEntity("id1", "Book One", List.of("Author X"), List.of("Genre A"), "Desc A", 10.0, 5),
                new BookEntity("id2", "Book Two", List.of("Author Y"), List.of("Genre B"), "Desc B", 20.0, 10)
        );
        when(bookRepositoryPort.findAllByIds(bookIds)).thenReturn(expectedBooks);

        // Act
        List<BookEntity> actualBooks = bookUseCase.findAllByIds(bookIds);

        // Assert
        verify(bookRepositoryPort, times(1)).findAllByIds(bookIds);
        assertEquals(expectedBooks, actualBooks, "A lista de livros por IDs deve ser a mesma que a retornada pelo repositório.");
        assertEquals(2, actualBooks.size(), "A lista deve conter 2 livros.");
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando findAllByIds não encontra livros")
    void shouldReturnEmptyListWhenFindAllByIdsFindsNoBooks() {
        // Arrange
        List<String> bookIds = Arrays.asList("non-existent-id1", "non-existent-id2");
        when(bookRepositoryPort.findAllByIds(bookIds)).thenReturn(Collections.emptyList());

        // Act
        List<BookEntity> actualBooks = bookUseCase.findAllByIds(bookIds);

        // Assert
        verify(bookRepositoryPort, times(1)).findAllByIds(bookIds);
        assertTrue(actualBooks.isEmpty(), "A lista de livros por IDs deve ser vazia.");
    }
}