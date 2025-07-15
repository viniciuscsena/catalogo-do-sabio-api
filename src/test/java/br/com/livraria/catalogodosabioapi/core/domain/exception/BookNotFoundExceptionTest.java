package br.com.livraria.catalogodosabioapi.core.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // Incluído conforme solicitado, embora não usado para mocks aqui.
class BookNotFoundExceptionTest {

    @Test
    @DisplayName("Deve criar a exceção com a mensagem e ID corretos")
    void shouldCreateExceptionWithCorrectMessageAndId() {
        // Arrange
        String bookId = "abc-123";
        String expectedMessage = "Livro não encontrado com o id: " + bookId;

        // Act
        BookNotFoundException exception = new BookNotFoundException(bookId);

        // Assert
        // Verifica se a mensagem da exceção está correta
        assertEquals(expectedMessage, exception.getMessage(), "A mensagem da exceção deve estar correta.");
        // Verifica se o ID do livro está correto
        assertEquals(bookId, exception.getId(), "O ID do livro na exceção deve ser o mesmo passado no construtor.");
    }

    @Test
    @DisplayName("Deve definir o timestamp ao criar a exceção")
    void shouldSetTimestampOnCreation() {
        // Arrange
        String bookId = "def-456";

        // Act
        LocalDateTime beforeCreation = LocalDateTime.now(); // Captura o tempo antes da criação
        BookNotFoundException exception = new BookNotFoundException(bookId);
        LocalDateTime afterCreation = LocalDateTime.now();  // Captura o tempo depois da criação

        // Assert
        // Verifica se o timestamp não é nulo
        assertNotNull(exception.getTimestamp(), "O timestamp não deve ser nulo.");
        // Verifica se o timestamp está dentro do intervalo de tempo esperado
        assertTrue(exception.getTimestamp().isAfter(beforeCreation.minusSeconds(1)) &&
                        exception.getTimestamp().isBefore(afterCreation.plusSeconds(1)),
                "O timestamp deve ser aproximadamente o momento da criação.");
    }
}