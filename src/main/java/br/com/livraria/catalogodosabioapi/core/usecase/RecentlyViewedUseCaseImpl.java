package br.com.livraria.catalogodosabioapi.core.usecase;

import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.BookUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.in.RecentlyViewedUseCase;
import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.RecentlyViewedPort;

import java.util.Collections;
import java.util.List;

public class RecentlyViewedUseCaseImpl implements RecentlyViewedUseCase {

    private final RecentlyViewedPort recentlyViewedPort;
    private final BookUseCase bookUseCase;

    public RecentlyViewedUseCaseImpl(RecentlyViewedPort recentlyViewedPort, BookUseCase bookUseCase) {
        this.recentlyViewedPort = recentlyViewedPort;
        this.bookUseCase = bookUseCase;
    }

    @Override
    public void track(String clientId, String bookId) {
        if (clientId != null && !clientId.isEmpty()) {
            recentlyViewedPort.save(clientId, bookId);
        }
    }

    @Override
    public List<BookEntity> find(String clientId) {

        if (clientId == null || clientId.isEmpty()){
            return Collections.emptyList();
        }

        List<String> bookIds = recentlyViewedPort.findByClientId(clientId);
        if (bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        return bookUseCase.findAllByIds(bookIds);
    }
}
