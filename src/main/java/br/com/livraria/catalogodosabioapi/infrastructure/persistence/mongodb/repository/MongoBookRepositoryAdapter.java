package br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.repository;

import br.com.livraria.catalogodosabioapi.core.domain.Book;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.BookRepositoryPort;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.document.BookDocument;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.mapper.BookDocumentMapper;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoBookRepositoryAdapter implements BookRepositoryPort {

    private final SpringDataBookMongoRepository springDataBookMongoRepository;
    private final BookDocumentMapper bookDocumentMapper;

    @Override
    public List<Book> findAll() {
        List<BookDocument> books = springDataBookMongoRepository.findAll();
        return bookDocumentMapper.toDomain(books);
    }

    @Override
    public Optional<Book> findById(String id) {
        Optional<BookDocument> bookDocumentOptional = springDataBookMongoRepository.findById(id);
        return bookDocumentOptional.map(bookDocumentMapper::toDomain);
    }

    @Override
    public List<Book> findByGenre(String genre) {
        List<BookDocument> books = springDataBookMongoRepository.findByGenresContaining(genre);
        return bookDocumentMapper.toDomain(books);
    }

    @Override
    public List<Book> findByAuthor(String author) {
        List<BookDocument> books = springDataBookMongoRepository.findByAuthorsContaining(author);
        return bookDocumentMapper.toDomain(books);
    }
}
