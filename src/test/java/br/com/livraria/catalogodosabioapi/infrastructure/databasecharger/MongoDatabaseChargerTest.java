package br.com.livraria.catalogodosabioapi.infrastructure.databasecharger;

import br.com.livraria.catalogodosabioapi.infrastructure.configuration.AiStudioProperties;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.document.BookDocument;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.repository.SpringDataBookMongoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoDatabaseChargerTest {

    @Mock
    private SpringDataBookMongoRepository bookRepository;
    @Mock
    private WebClient webClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private AiStudioProperties aiStudioProperties;

    @InjectMocks
    private MongoDatabaseCharger mongoDatabaseCharger;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RequestBodySpec requestBodySpec;
    @Mock
    private RequestHeadersSpec requestHeadersSpec;
    @Mock
    private ResponseSpec responseSpec;

    private final String API_KEY = "test-api-key";
    private final String API_URL = "http://test.ai.studio/api";
    private final String SCHEMA_JSON = "{\"type\": \"ARRAY\", \"items\": {\"type\": \"OBJECT\"}}"; // Exemplo de schema


    void mockWebClient() throws IOException {
        // Configuração padrão para o WebClient mock
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(anyMap())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.empty()); // Default para Mono vazio

        // Configuração padrão para AiStudioProperties
        when(aiStudioProperties.apiKey()).thenReturn(API_KEY);
        when(aiStudioProperties.apiUrl()).thenReturn(API_URL);

        // Mocking do InputStream para requestSchema()
        Resource mockResource = mock(Resource.class);
        when(aiStudioProperties.requestSchema()).thenReturn(mockResource);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(SCHEMA_JSON.getBytes()));

        // Configuração padrão para ObjectMapper.readValue para o schema
        when(objectMapper.readValue(any(InputStream.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(Map.of("type", "ARRAY", "items", Map.of("type", "OBJECT")));
    }

    @Test
    @DisplayName("Não deve carregar dados se o banco de dados já contém livros")
    void shouldNotLoadDataIfDatabaseHasBooks() throws Exception {
        // Arrange
        when(bookRepository.count()).thenReturn(1L); // Simula DB com dados

        // Act
        mongoDatabaseCharger.run();

        // Assert
        verify(bookRepository, times(1)).count(); // Verifica se count foi chamado
        verify(aiStudioProperties, never()).apiKey(); // Não deve tentar obter a API Key
        verify(webClient, never()).post(); // Não deve chamar a API de IA
        verify(bookRepository, never()).saveAll(anyList()); // Não deve salvar dados
    }

    @Test
    @DisplayName("Não deve carregar dados se a API Key do Google AI Studio não está configurada")
    void shouldNotLoadDataIfApiKeyIsNotConfigured() throws Exception {
        // Arrange
        when(bookRepository.count()).thenReturn(0L); // Simula DB vazio
        when(aiStudioProperties.apiKey()).thenReturn(null); // Simula API Key nula

        // Act
        mongoDatabaseCharger.run();

        // Assert
        verify(bookRepository, times(1)).count();
        verify(aiStudioProperties, times(1)).apiKey(); // Deve verificar a API Key
        verify(webClient, never()).post(); // Não deve chamar a API de IA
        verify(bookRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Não deve carregar dados se a API Key do Google AI Studio está em branco")
    void shouldNotLoadDataIfApiKeyIsBlank() throws Exception {
        // Arrange
        when(bookRepository.count()).thenReturn(0L);
        when(aiStudioProperties.apiKey()).thenReturn("   "); // Simula API Key em branco

        // Act
        mongoDatabaseCharger.run();

        // Assert
        verify(bookRepository, times(1)).count();
        verify(aiStudioProperties, times(2)).apiKey();
        verify(webClient, never()).post();
        verify(bookRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve carregar e salvar livros com sucesso da API de IA")
    void shouldLoadAndSaveBooksSuccessfullyFromAiApi() throws Exception {
        // Arrange
        mockWebClient();
        when(bookRepository.count()).thenReturn(0L); // DB vazio

        // Simula uma resposta JSON válida da IA
        ObjectMapper realObjectMapper = new ObjectMapper(); // Usar um ObjectMapper real para construir o JsonNode
        ObjectNode aiResponseRoot = realObjectMapper.createObjectNode();
        ObjectNode candidatesNode = realObjectMapper.createObjectNode();
        ArrayNode partsNode = realObjectMapper.createArrayNode();
        ObjectNode partNode = realObjectMapper.createObjectNode();

        ObjectNode book1Node = realObjectMapper.createObjectNode();
        book1Node.put("id", "1");
        book1Node.put("title", "Book 1");
        ArrayNode authors1Array = realObjectMapper.createArrayNode();
        authors1Array.add("Author 1");
        book1Node.set("authors", authors1Array);
        ArrayNode genres1Array = realObjectMapper.createArrayNode();
        genres1Array.add("Genre A");
        book1Node.set("genres", genres1Array);

        ObjectNode book2Node = realObjectMapper.createObjectNode();
        book2Node.put("id", "2");
        book2Node.put("title", "Book 2");
        ArrayNode authors2Array = realObjectMapper.createArrayNode();
        authors2Array.add("Author 2");
        book2Node.set("authors", authors2Array);
        ArrayNode genres2Array = realObjectMapper.createArrayNode();
        genres2Array.add("Genre B");
        book2Node.set("genres", genres2Array);

        ArrayNode booksArrayNode = realObjectMapper.createArrayNode();
        booksArrayNode.add(book1Node);
        booksArrayNode.add(book2Node);

        ObjectNode booksWrapperNode = realObjectMapper.createObjectNode();
        booksWrapperNode.set("books", booksArrayNode);

        String booksJsonString = realObjectMapper.writeValueAsString(booksWrapperNode);


        partNode.put("text", booksJsonString);
        partsNode.add(partNode);
        candidatesNode.set("content", realObjectMapper.createObjectNode().set("parts", partsNode));
        aiResponseRoot.set("candidates", realObjectMapper.createArrayNode().add(candidatesNode));

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(aiResponseRoot));

        // Mock para o objectMapper.readTree e objectMapper.convertValue
        // Quando parseApiResponse chama readTree no jsonString extraído
        when(objectMapper.readTree(booksJsonString)).thenReturn(realObjectMapper.readTree(booksJsonString));
        // Quando parseApiResponse chama convertValue
        when(objectMapper.convertValue(any(JsonNode.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn
                        (List.of(
                        new BookDocument("1", "Book 1", List.of("Author 1"), List.of("Genre A"), null, null, null),
                        new BookDocument("2", "Book 2", List.of("Author 2"), List.of("Genre B"), null, null, null)
                ));

        // Act
        mongoDatabaseCharger.run();

        // Assert
        verify(bookRepository, times(1)).count();
        verify(aiStudioProperties, times(3)).apiKey();
        verify(webClient, times(1)).post(); // Garante que a chamada à API foi feita
        verify(bookRepository, times(1)).saveAll(anyList()); // Garante que os livros foram salvos
        // Captura o argumento passado para saveAll para inspeção detalhada
        ArgumentCaptor<List<BookDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(bookRepository).saveAll(captor.capture());
        List<BookDocument> savedBooks = captor.getValue();
        assertFalse(savedBooks.isEmpty());
        assertEquals(2, savedBooks.size());
        assertEquals("Book 1", savedBooks.get(0).getTitle());
    }

    @Test
    @DisplayName("Deve lidar com falha na comunicação com a API de IA")
    void shouldHandleAiApiCommunicationFailure() throws Exception {
        mockWebClient();
        // Arrange
        when(bookRepository.count()).thenReturn(0L);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.error(new RuntimeException("Network error")));

        // Act
        mongoDatabaseCharger.run();

        // Assert
        verify(bookRepository, times(1)).count();
        verify(webClient, times(1)).post();
        verify(bookRepository, never()).saveAll(anyList()); // Não deve salvar nada
    }

    @Test
    @DisplayName("Deve lidar com resposta da API de IA sem o nó 'text' esperado")
    void shouldHandleAiApiMissingTextNode() throws Exception {
        // Arrange
        mockWebClient();
        when(bookRepository.count()).thenReturn(0L);

        ObjectMapper realObjectMapper = new ObjectMapper();
        ObjectNode aiResponseRoot = realObjectMapper.createObjectNode();
        ObjectNode candidatesNode = realObjectMapper.createObjectNode();
        ArrayNode partsNode = realObjectMapper.createArrayNode();
        ObjectNode partNode = realObjectMapper.createObjectNode();
        partsNode.add(partNode);
        candidatesNode.set("content", realObjectMapper.createObjectNode().set("parts", partsNode));
        aiResponseRoot.set("candidates", realObjectMapper.createArrayNode().add(candidatesNode));

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(aiResponseRoot));

        // Act
        mongoDatabaseCharger.run();

        // Assert
        verify(bookRepository, times(1)).count();
        verify(webClient, times(1)).post();
        verify(bookRepository, never()).saveAll(anyList()); // Não deve salvar nada
    }

    @Test
    @DisplayName("Deve lidar com resposta da API de IA com JSON malformado dentro do nó 'text'")
    void shouldHandleAiApiMalformedJsonInTextNode() throws Exception {
        // Arrange
        mockWebClient();
        when(bookRepository.count()).thenReturn(0L);

        ObjectMapper realObjectMapper = new ObjectMapper();
        ObjectNode aiResponseRoot = realObjectMapper.createObjectNode();
        ObjectNode candidatesNode = realObjectMapper.createObjectNode();
        ArrayNode partsNode = realObjectMapper.createArrayNode();
        ObjectNode partNode = realObjectMapper.createObjectNode();
        partNode.put("text", "{invalid json"); // JSON malformado
        partsNode.add(partNode);
        candidatesNode.set("content", realObjectMapper.createObjectNode().set("parts", partsNode));
        aiResponseRoot.set("candidates", realObjectMapper.createArrayNode().add(candidatesNode));

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(aiResponseRoot));
        doThrow(new JsonMappingException("Malformed JSON")).when(objectMapper).readTree(eq("{invalid json"));


        // Act
        mongoDatabaseCharger.run();

        // Assert
        verify(bookRepository, times(1)).count();
        verify(webClient, times(1)).post();
        verify(bookRepository, never()).saveAll(anyList()); // Não deve salvar nada
    }

    @Test
    @DisplayName("Deve lidar com resposta da API de IA sem o nó 'books' esperado")
    void shouldHandleAiApiMissingBooksNode() throws Exception {
        // Arrange
        mockWebClient();
        when(bookRepository.count()).thenReturn(0L);

        ObjectMapper realObjectMapper = new ObjectMapper();
        ObjectNode aiResponseRoot = realObjectMapper.createObjectNode();
        ObjectNode candidatesNode = realObjectMapper.createObjectNode();
        ArrayNode partsNode = realObjectMapper.createArrayNode();
        ObjectNode partNode = realObjectMapper.createObjectNode();

        String jsonWithoutBooks = realObjectMapper.writeValueAsString(
                realObjectMapper.createObjectNode()
                        .put("someOtherField", "value") // Campo diferente de "books"
        );
        partNode.put("text", jsonWithoutBooks);
        partsNode.add(partNode);
        candidatesNode.set("content", realObjectMapper.createObjectNode().set("parts", partsNode));
        aiResponseRoot.set("candidates", realObjectMapper.createArrayNode().add(candidatesNode));

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(aiResponseRoot));
        when(objectMapper.readTree(jsonWithoutBooks)).thenReturn(realObjectMapper.readTree(jsonWithoutBooks));


        // Act
        mongoDatabaseCharger.run();

        // Assert
        verify(bookRepository, times(1)).count();
        verify(webClient, times(1)).post();
        verify(bookRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve lidar com resposta da API de IA com nó 'books' não sendo um array")
    void shouldHandleAiApiBooksNodeNotArray() throws Exception {
        // Arrange
        mockWebClient();
        when(bookRepository.count()).thenReturn(0L);

        ObjectMapper realObjectMapper = new ObjectMapper();
        ObjectNode aiResponseRoot = realObjectMapper.createObjectNode();
        ObjectNode candidatesNode = realObjectMapper.createObjectNode();
        ArrayNode partsNode = realObjectMapper.createArrayNode();
        ObjectNode partNode = realObjectMapper.createObjectNode();

        // JSON string que a IA retornaria, com "books" como objeto e não array
        String jsonBooksNotArray = realObjectMapper.writeValueAsString(
                realObjectMapper.createObjectNode()
                        .set("books", realObjectMapper.createObjectNode().put("id", "single-book"))
        );
        partNode.put("text", jsonBooksNotArray);
        partsNode.add(partNode);
        candidatesNode.set("content", realObjectMapper.createObjectNode().set("parts", partsNode));
        aiResponseRoot.set("candidates", realObjectMapper.createArrayNode().add(candidatesNode));

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(aiResponseRoot));
        when(objectMapper.readTree(jsonBooksNotArray)).thenReturn(realObjectMapper.readTree(jsonBooksNotArray));


        // Act
        mongoDatabaseCharger.run();

        // Assert
        verify(bookRepository, times(1)).count();
        verify(webClient, times(1)).post();
        verify(bookRepository, never()).saveAll(anyList()); // Não deve salvar nada
    }

    @Test
    @DisplayName("Deve lidar com resposta da API de IA que retorna lista vazia de livros")
    void shouldHandleAiApiReturnsEmptyBookList() throws Exception {
        // Arrange
        mockWebClient();
        when(bookRepository.count()).thenReturn(0L);

        ObjectMapper realObjectMapper = new ObjectMapper();
        ObjectNode aiResponseRoot = realObjectMapper.createObjectNode();
        ObjectNode candidatesNode = realObjectMapper.createObjectNode();
        ArrayNode partsNode = realObjectMapper.createArrayNode();
        ObjectNode partNode = realObjectMapper.createObjectNode();

        // JSON string que a IA retornaria, com array "books" vazio
        String emptyBooksJsonString = realObjectMapper.writeValueAsString(
                realObjectMapper.createObjectNode()
                        .set("books", realObjectMapper.createArrayNode()) // Array vazio
        );
        partNode.put("text", emptyBooksJsonString);
        partsNode.add(partNode);
        candidatesNode.set("content", realObjectMapper.createObjectNode().set("parts", partsNode));
        aiResponseRoot.set("candidates", realObjectMapper.createArrayNode().add(candidatesNode));

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(aiResponseRoot));
        when(objectMapper.readTree(emptyBooksJsonString)).thenReturn(realObjectMapper.readTree(emptyBooksJsonString));
        // Quando convertValue é chamado para um array vazio, ele retorna uma lista vazia
        when(objectMapper.convertValue(any(JsonNode.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(Collections.emptyList());

        // Act
        mongoDatabaseCharger.run();

        // Assert
        verify(bookRepository, times(1)).count();
        verify(webClient, times(1)).post();
        verify(bookRepository, never()).saveAll(anyList()); // Não deve salvar nada se a lista de livros estiver vazia
    }
}
