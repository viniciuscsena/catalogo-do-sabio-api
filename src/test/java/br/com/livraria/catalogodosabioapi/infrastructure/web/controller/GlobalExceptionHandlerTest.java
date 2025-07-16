package br.com.livraria.catalogodosabioapi.infrastructure.web.controller;

import br.com.livraria.catalogodosabioapi.core.domain.exception.BookNotFoundException;
import br.com.livraria.catalogodosabioapi.model.ApiError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;


    @Test
    @DisplayName("handleBookNotFoundException: Deve retornar HttpStatus.NOT_FOUND com ApiError correto")
    void handleBookNotFoundException_shouldReturnNotFoundApiError() {
        // Arrange
        String bookId = "non-existent-book-id";
        BookNotFoundException ex = new BookNotFoundException(bookId);
        LocalDateTime exceptionTimestamp = ex.getTimestamp(); // Captura o timestamp da exceção

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleBookNotFoundException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "O status HTTP deve ser 404 NOT FOUND.");

        ApiError apiError = response.getBody();
        assertNotNull(apiError, "O corpo da resposta ApiError não deve ser nulo.");
        assertEquals(HttpStatus.NOT_FOUND.value(), apiError.getStatus(), "O status do erro no corpo deve ser 404.");
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), apiError.getError(), "A frase de erro deve ser 'Not Found'.");
        assertEquals("Livro não encontrado com o id: " + bookId, apiError.getMessage(), "A mensagem de erro deve ser a da exceção.");

        assertNotNull(apiError.getTimestamp(), "O timestamp do ApiError não deve ser nulo.");
        assertEquals(exceptionTimestamp.atOffset(ZoneOffset.UTC), apiError.getTimestamp(), "O timestamp deve corresponder ao da exceção, ajustado para UTC.");
    }

    @Test
    @DisplayName("handleGlobalException: Deve retornar HttpStatus.INTERNAL_SERVER_ERROR com ApiError correto")
    void handleGlobalException_shouldReturnInternalServerErrorApiError() {
        // Arrange
        String errorMessage = "Algo deu muito errado!";
        Exception ex = new RuntimeException(errorMessage);

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleGlobalException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "O status HTTP deve ser 500 INTERNAL SERVER ERROR.");

        ApiError apiError = response.getBody();
        assertNotNull(apiError, "O corpo da resposta ApiError não deve ser nulo.");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), apiError.getStatus(), "O status do erro no corpo deve ser 500.");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), apiError.getError(), "A frase de erro deve ser 'Internal Server Error'.");
        assertEquals("Ocorreu um erro inesperado no servidor. Por favor, tente novamente mais tarde.", apiError.getMessage(), "A mensagem de erro deve ser a genérica.");

        assertNotNull(apiError.getTimestamp(), "O timestamp do ApiError não deve ser nulo.");
        assertTrue(apiError.getTimestamp().isAfter(OffsetDateTime.now().minusSeconds(5)), "O timestamp deve ser recente.");
        assertTrue(apiError.getTimestamp().isBefore(OffsetDateTime.now().plusSeconds(1)), "O timestamp deve ser recente.");
    }
}
