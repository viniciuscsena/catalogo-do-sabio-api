package br.com.livraria.catalogodosabioapi.infrastructure.web.controller;

import br.com.livraria.catalogodosabioapi.api.BooksApi;
import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.BookUseCase;
import br.com.livraria.catalogodosabioapi.infrastructure.web.mapper.BookApiMapper;
import br.com.livraria.catalogodosabioapi.model.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1")
public class BookController implements BooksApi {
    private final BookUseCase bookUseCase;

    private final BookApiMapper bookApiMapper;

    @Override
    public ResponseEntity<Book> booksIdGet(String id, UUID xClientID) {
        log.info("Buscando livro por ID: {}", id);
        BookEntity bookEntity = bookUseCase.findById(id);
        Book book = bookApiMapper.toApi(bookEntity);
        log.info("Busca por ID {} finalizada com sucesso.", id);
        return ResponseEntity.ok(book);
    }

    @Override
    public ResponseEntity<List<Book>> booksGet() {
        log.info("Buscando todos os livros.");
        List<BookEntity> bookEntityList = bookUseCase.findAll();
        List<Book> bookList = bookApiMapper.toApi(bookEntityList);
        log.info("Busca por todos os livros finalizada. {} livros encontrados.", bookList.size());
        return ResponseEntity.ok(bookList);
    }

    @Override
    public ResponseEntity<List<Book>> booksAuthorAuthorGet(String author) {
        log.info("Buscando livros por autor: '{}'", author);
        List<BookEntity> bookEntityList = bookUseCase.findByAuthor(author);
        List<Book> bookList = bookApiMapper.toApi(bookEntityList);
        log.info("Busca por autor '{}' finalizada. {} livros encontrados.", author, bookList.size());
        return ResponseEntity.ok(bookList);
    }

    @Override
    public ResponseEntity<List<Book>> booksGenreGenreGet(String genre) {
        log.info("Buscando livros por gênero: '{}'", genre);
        List<BookEntity> bookEntityList = bookUseCase.findByGenre(genre);
        List<Book> bookList = bookApiMapper.toApi(bookEntityList);
        log.info("Busca por gênero '{}' finalizada. {} livros encontrados.", genre, bookList.size());
        return ResponseEntity.ok(bookList);
    }
}