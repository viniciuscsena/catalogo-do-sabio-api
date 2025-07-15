package br.com.livraria.catalogodosabioapi.core.usecase.boundary.out;

import java.util.List;
import java.util.Optional;

import br.com.livraria.catalogodosabioapi.core.domain.Book;

public interface BookRepositoryPort {

    List<Book> findAll();
    Optional<Book> findById(String id);
    List<Book> findByGenre(String genre);
    List<Book> findByAuthor(String author);
}
