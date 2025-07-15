package br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.document.BookDocument;

@Repository
public interface SpringDataBookMongoRepository extends MongoRepository<BookDocument, String> {

    @Query(value = "{ 'genres': ?0 }", collation = "{ 'locale': 'pt', 'strength': 1 }")
    List<BookDocument> findByGenresContaining(String genre);

    @Query(value = "{ 'authors': ?0 }", collation = "{ 'locale': 'pt', 'strength': 1 }")
    List<BookDocument> findByAuthorsContaining(String author);
}
