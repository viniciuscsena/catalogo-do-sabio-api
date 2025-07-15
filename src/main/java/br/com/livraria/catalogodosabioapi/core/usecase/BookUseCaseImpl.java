package br.com.livraria.catalogodosabioapi.core.usecase;

import java.util.List;
import java.util.Optional;

import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;
import br.com.livraria.catalogodosabioapi.core.domain.exception.BookNotFoundException;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.BookUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.BookRepositoryPort;

public class BookUseCaseImpl implements BookUseCase {

    private final BookRepositoryPort bookRepositoryPort;

    public BookUseCaseImpl(BookRepositoryPort bookRepositoryPort) {
        this.bookRepositoryPort = bookRepositoryPort;
    }

    @Override
    public List<BookEntity> findAll() {
        return bookRepositoryPort.findAll();
    }

    @Override
    public BookEntity findById(String id) {
        Optional<BookEntity> bookOptional = bookRepositoryPort.findById(id);
        return bookOptional.orElseThrow(() -> new BookNotFoundException(id));
    }

    @Override
    public List<BookEntity> findByGenre(String genre) {
        return bookRepositoryPort.findByGenre(genre);
    }

    @Override
    public List<BookEntity> findByAuthor(String author) {
        return bookRepositoryPort.findByAuthor(author);
    }

    @Override
    public List<BookEntity> findAllByIds(List<String> ids) {
        return bookRepositoryPort.findAllByIds(ids);
    }
}
