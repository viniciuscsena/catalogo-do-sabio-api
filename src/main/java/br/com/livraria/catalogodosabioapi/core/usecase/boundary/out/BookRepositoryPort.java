package br.com.livraria.catalogodosabioapi.core.usecase.boundary.out;

import java.util.List;
import java.util.Optional;

import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;

public interface BookRepositoryPort {

    List<BookEntity> findAll();
    Optional<BookEntity> findById(String id);
    List<BookEntity> findByGenre(String genre);
    List<BookEntity> findByAuthor(String author);
    List<BookEntity> findAllByIds(List<String> ids);
}
