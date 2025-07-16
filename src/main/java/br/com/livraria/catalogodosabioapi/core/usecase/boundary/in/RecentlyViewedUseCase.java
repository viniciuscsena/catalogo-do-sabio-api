package br.com.livraria.catalogodosabioapi.core.usecase.boundary.in;

import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;

import java.util.List;

public interface RecentlyViewedUseCase {

    void track(String clientID, String bookId);

    List<BookEntity> find(String clientId);
}
