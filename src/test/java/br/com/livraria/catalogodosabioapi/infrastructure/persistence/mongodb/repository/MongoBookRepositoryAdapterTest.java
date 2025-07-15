package br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.repository;

import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.document.BookDocument;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.mapper.BookDocumentMapper;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.mapper.BookDocumentMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoBookRepositoryAdapterTest {

    @Mock
    private SpringDataBookMongoRepository springDataBookMongoRepository;

    private BookDocumentMapper bookDocumentMapper = new BookDocumentMapperImpl();;

    private MongoBookRepositoryAdapter mongoBookRepositoryAdapter;

    // Entidades de domínio e documentos mock para uso nos testes
    private BookDocument mockBookDocument;
    private BookDocument mockBookDocument2;


    @BeforeEach
    void setUp() {
        mongoBookRepositoryAdapter = new MongoBookRepositoryAdapter( springDataBookMongoRepository, bookDocumentMapper);

        mockBookDocument = new BookDocument("1", "Test Book", List.of("Author A"), List.of("Genre X"), "Desc", 10.0, 5);
        mockBookDocument2 = new BookDocument("2", "Another Book", List.of("Author B"), List.of("Genre Y"), "Desc2", 20.0, 10);
    }

    @Test
    @DisplayName("Deve retornar todos os livros do repositório e mapeá-los para entidades de domínio")
    void shouldFindAllBooksAndMapToDomainEntities() {
        // Arrange
        List<BookDocument> documents = Arrays.asList(mockBookDocument, mockBookDocument2);
        List<BookEntity> expectedEntities = bookDocumentMapper.toDomain(documents);

        when(springDataBookMongoRepository.findAll()).thenReturn(documents);

        // Act
        List<BookEntity> result = mongoBookRepositoryAdapter.findAll();

        // Assert
        verify(springDataBookMongoRepository, times(1)).findAll();
        assertEquals(expectedEntities, result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve retornar um livro por ID e mapeá-lo para entidade de domínio quando encontrado")
    void shouldFindBookByIdAndMapToDomainEntityWhenFound() {
        // Arrange
        String id = "1";
        when(springDataBookMongoRepository.findById(id)).thenReturn(Optional.of(mockBookDocument));
        BookEntity expectedEntity = bookDocumentMapper.toDomain(mockBookDocument);

        // Act
        Optional<BookEntity> result = mongoBookRepositoryAdapter.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedEntity, result.get());
        verify(springDataBookMongoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando livro por ID não é encontrado")
    void shouldReturnEmptyOptionalWhenBookByIdNotFound() {
        // Arrange
        String id = "non-existent";
        when(springDataBookMongoRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<BookEntity> result = mongoBookRepositoryAdapter.findById(id);

        // Assert
        assertTrue(result.isEmpty());
        verify(springDataBookMongoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve retornar livros por gênero e mapeá-los para entidades de domínio")
    void shouldFindBooksByGenreAndMapToDomainEntities() {
        // Arrange
        String genre = "Fiction";
        List<BookDocument> documents = Arrays.asList(mockBookDocument);
        List<BookEntity> expectedEntities = bookDocumentMapper.toDomain(documents); // Resultado do mapper real

        when(springDataBookMongoRepository.findByGenresContaining(genre)).thenReturn(documents);

        // Act
        List<BookEntity> result = mongoBookRepositoryAdapter.findByGenre(genre);

        // Assert
        verify(springDataBookMongoRepository, times(1)).findByGenresContaining(genre);
        assertEquals(expectedEntities, result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum livro é encontrado por gênero")
    void shouldReturnEmptyListWhenNoBooksFoundByGenre() {
        // Arrange
        String genre = "NonExistent";
        when(springDataBookMongoRepository.findByGenresContaining(genre)).thenReturn(Collections.emptyList());

        // Act
        List<BookEntity> result = mongoBookRepositoryAdapter.findByGenre(genre);

        // Assert
        assertTrue(result.isEmpty());
        verify(springDataBookMongoRepository, times(1)).findByGenresContaining(genre);
    }

    @Test
    @DisplayName("Deve retornar livros por autor e mapeá-los para entidades de domínio")
    void shouldFindBooksByAuthorAndMapToDomainEntities() {
        // Arrange
        String author = "Author A";
        List<BookDocument> documents = Arrays.asList(mockBookDocument);
        List<BookEntity> expectedEntities = bookDocumentMapper.toDomain(documents); // Resultado do mapper real

        when(springDataBookMongoRepository.findByAuthorsContaining(author)).thenReturn(documents);
        // REMOVIDO: when(bookDocumentMapper.toDomain(documents)).thenReturn(entities);

        // Act
        List<BookEntity> result = mongoBookRepositoryAdapter.findByAuthor(author);

        // Assert
        verify(springDataBookMongoRepository, times(1)).findByAuthorsContaining(author);
        assertEquals(expectedEntities, result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum livro é encontrado por autor")
    void shouldReturnEmptyListWhenNoBooksFoundByAuthor() {
        // Arrange
        String author = "NonExistent";
        when(springDataBookMongoRepository.findByAuthorsContaining(author)).thenReturn(Collections.emptyList());

        // Act
        List<BookEntity> result = mongoBookRepositoryAdapter.findByAuthor(author);

        // Assert
        assertTrue(result.isEmpty());
        verify(springDataBookMongoRepository, times(1)).findByAuthorsContaining(author);
    }

    @Test
    @DisplayName("Deve retornar livros por lista de IDs e mapeá-los para entidades de domínio")
    void shouldFindAllByIdsAndMapToDomainEntities() {
        // Arrange
        List<String> ids = Arrays.asList("1", "2");
        List<BookDocument> documents = Arrays.asList(mockBookDocument, mockBookDocument2);
        List<BookEntity> expectedEntities = bookDocumentMapper.toDomain(documents);

        when(springDataBookMongoRepository.findAllById(ids)).thenReturn(documents);

        // Act
        List<BookEntity> result = mongoBookRepositoryAdapter.findAllByIds(ids);

        // Assert
        verify(springDataBookMongoRepository, times(1)).findAllById(ids);
        assertEquals(expectedEntities, result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum livro é encontrado por lista de IDs")
    void shouldReturnEmptyListWhenNoBooksFoundByIds() {
        // Arrange
        List<String> ids = Arrays.asList("non-existent1", "non-existent2");
        when(springDataBookMongoRepository.findAllById(ids)).thenReturn(Collections.emptyList());

        // Act
        List<BookEntity> result = mongoBookRepositoryAdapter.findAllByIds(ids);

        // Assert
        assertTrue(result.isEmpty());
        verify(springDataBookMongoRepository, times(1)).findAllById(ids);
    }
}
