package br.com.livraria.catalogodosabioapi.core.domain.exception;

import java.time.LocalDateTime;

public class BookNotFoundException extends RuntimeException {

    private final LocalDateTime timestamp;

    public BookNotFoundException(String id) {
        super("Livro não encontrado com o id: " + id);
        this.timestamp = LocalDateTime.now();
    }

}
