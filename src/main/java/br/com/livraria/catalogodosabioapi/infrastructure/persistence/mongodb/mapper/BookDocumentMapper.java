package br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.mapper;


import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.document.BookDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookDocumentMapper {

    BookEntity toDomain(BookDocument bookDocument);

    List<BookEntity> toDomain(List<BookDocument> bookDocuments);
}
