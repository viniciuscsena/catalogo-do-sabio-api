package br.com.livraria.catalogodosabioapi.core.usecase.boundary.out;

import java.util.List;

public interface RecentlyViewedPort {

    void save(String clientId, String bookId);

    List<String> findByClientId(String clientId);
}
