package br.com.livraria.catalogodosabioapi.core.domain.exception;

import java.time.LocalDateTime;

public class BookNotFoundException extends RuntimeException {

    private final LocalDateTime timestamp;
    private final String id;

    public BookNotFoundException(String id) {
        super("Livro não encontrado com o id: " + id);
        this.id = id;
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp(){
        return this.timestamp;
    }

    public String getId(){
        return this.id;
    }
}
