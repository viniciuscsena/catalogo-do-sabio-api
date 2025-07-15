package br.com.livraria.catalogodosabioapi.infrastructure.web.mapper;

import br.com.livraria.catalogodosabioapi.core.domain.BookEntity;
import br.com.livraria.catalogodosabioapi.model.Book;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookApiMapper {

    Book toApi(BookEntity bookEntityDomain);
    List<Book> toApi(List<BookEntity> bookEntityDomainList);
}
