package br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.document;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "books")
@CompoundIndex(name = "genre_collation_idx", def = "{'genres': 1}", collation = "{'locale':'pt', 'strength':1}")
@CompoundIndex(name = "author_collation_idx", def = "{'authors': 1}", collation = "{'locale':'pt', 'strength':1}")
public class BookDocument {

    @Id
    private String id;

    private String title;
    private List<String> authors;
    private List<String> genres;
    private String description;
    private Double price;
    private Integer stock;
}   
