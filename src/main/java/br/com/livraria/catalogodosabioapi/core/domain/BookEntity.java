package br.com.livraria.catalogodosabioapi.core.domain;

import java.io.Serializable;
import java.util.List;

public record BookEntity (
    String id,
    String title,
    List<String> authors,
    List<String> genres,
    String description,
    Double price,
    Integer stock
)implements Serializable {
}
