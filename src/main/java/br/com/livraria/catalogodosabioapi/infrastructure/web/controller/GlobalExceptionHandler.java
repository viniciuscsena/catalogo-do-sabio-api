package br.com.livraria.catalogodosabioapi.infrastructure.web.controller;


import br.com.livraria.catalogodosabioapi.core.domain.exception.BookNotFoundException;
import br.com.livraria.catalogodosabioapi.model.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ApiError> handleBookNotFoundException(BookNotFoundException ex){
        log.warn("Tentativa de acesso ao livro com id {}. Livro não encontrado", ex.getId());

        ApiError apiError = new ApiError()
                .timestamp(ex.getTimestamp().atOffset(ZoneOffset.UTC))
                .message(ex.getMessage())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .status(HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(Exception ex) {
        log.error("Erro inesperado processando a requisição: {}", ex.getMessage());

        ApiError apiError = new ApiError()
                .timestamp(OffsetDateTime.now())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Ocorreu um erro inesperado no servidor. Por favor, tente novamente mais tarde.");

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
