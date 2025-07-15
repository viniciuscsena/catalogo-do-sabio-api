package br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.mapper;


import br.com.livraria.catalogodosabioapi.core.domain.Book;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.document.BookDocument;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookDocumentMapper {

    Book toDomain(BookDocument document);

    List<Book> toDomain(List<BookDocument> documents);

    BookDocument toDocument(Book book);
}
