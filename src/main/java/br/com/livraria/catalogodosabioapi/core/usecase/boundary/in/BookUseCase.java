package br.com.livraria.catalogodosabioapi.core.usecase.boundary.in;

import java.util.List;

import br.com.livraria.catalogodosabioapi.core.domain.Book;

public interface BookUseCase {

    List<Book> findAll();
    Book findById(String id);
    List<Book> findByGenre(String genre);
    List<Book> findByAuthor(String author);
}
