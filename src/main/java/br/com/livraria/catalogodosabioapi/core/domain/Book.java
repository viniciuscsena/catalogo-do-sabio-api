package br.com.livraria.catalogodosabioapi.core.domain;

import java.util.List;

public record Book (    
    String id,
    String title,
    List<String> authors,
    List<String> genres,
    String description,
    Double price,
    Integer stock
) {
}
