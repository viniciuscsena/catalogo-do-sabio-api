package br.com.livraria.catalogodosabioapi.core.usecase;

import java.util.List;
import java.util.Optional;

import br.com.livraria.catalogodosabioapi.core.domain.Book;
import br.com.livraria.catalogodosabioapi.core.domain.exception.BookNotFoundException;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.BookUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.BookRepositoryPort;

public class BookUseCaseImpl implements BookUseCase {

    private final BookRepositoryPort bookRepositoryPort;

    public BookUseCaseImpl(BookRepositoryPort bookRepositoryPort) {
        this.bookRepositoryPort = bookRepositoryPort;
    }

    @Override
    public List<Book> findAll() {
        return bookRepositoryPort.findAll();
    }

    @Override
    public Book findById(String id) {
        Optional<Book> bookOptional = bookRepositoryPort.findById(id);
        return bookOptional.orElseThrow(() -> new BookNotFoundException(id));
    }

    @Override
    public List<Book> findByGenre(String genre) {
        return bookRepositoryPort.findByGenre(genre);
    }

    @Override
    public List<Book> findByAuthor(String author) {
        return bookRepositoryPort.findByAuthor(author);
    }
}
