package br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.repository;

import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.BookRepositoryPort;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.document.BookDocument;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.mapper.BookDocumentMapper;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MongoBookRepositoryAdapter implements BookRepositoryPort {

    private final SpringDataBookMongoRepository springDataBookMongoRepository;
    private final BookDocumentMapper bookDocumentMapper;

    @Override
    public List<BookEntity> findAll() {
        List<BookDocument> books = springDataBookMongoRepository.findAll();
        return bookDocumentMapper.toDomain(books);
    }

    @Override
    public Optional<BookEntity> findById(String id) {
        log.debug("Buscando livro na base de dados pelo id: {}", id);
        Optional<BookDocument> bookDocumentOptional = springDataBookMongoRepository.findById(id);
        log.debug("Consulta por id {} Resultado: livro {}", id, bookDocumentOptional.isEmpty() ? "não encontrado" : "encontrado");
        return bookDocumentOptional.map(bookDocumentMapper::toDomain);
    }

    @Override
    public List<BookEntity> findByGenre(String genre) {
        log.debug("Buscando livros na base de dados pelo gênero {}", genre);
        List<BookDocument> books = springDataBookMongoRepository.findByGenresContaining(genre);
        log.debug("Consulta à base de dados pelo gênero '{}' retornou {} documentos.", genre, books.size());
        return bookDocumentMapper.toDomain(books);
    }

    @Override
    public List<BookEntity> findByAuthor(String author) {
        log.debug("Buscando livros na base de dados pelo autor {}", author);
        List<BookDocument> books = springDataBookMongoRepository.findByAuthorsContaining(author);
        log.debug("Consulta à base de dados pelo autor '{}' retornou {} documentos.", author, books.size());
        return bookDocumentMapper.toDomain(books);
    }
}
