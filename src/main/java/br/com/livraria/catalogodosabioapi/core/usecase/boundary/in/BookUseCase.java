package br.com.livraria.catalogodosabioapi.core.usecase.boundary.in;

import java.util.List;

import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;

public interface BookUseCase {

    List<BookEntity> findAll();
    BookEntity findById(String id);
    List<BookEntity> findByGenre(String genre);
    List<BookEntity> findByAuthor(String author);
    List<BookEntity> findAllByIds(List<String> ids);
}
