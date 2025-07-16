package br.com.livraria.catalogodosabioapi.infrastructure.databasecharger;

import br.com.livraria.catalogodosabioapi.infrastructure.configuration.AiStudioProperties;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.document.BookDocument;
import br.com.livraria.catalogodosabioapi.infrastructure.persistence.mongodb.repository.SpringDataBookMongoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Profile("dev")
@RequiredArgsConstructor
public class MongoDatabaseCharger implements CommandLineRunner {

    private final SpringDataBookMongoRepository bookRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final AiStudioProperties aiStudioProperties;


    @Override
    @Async
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            log.info("Banco de dados já contém dados. Nenhuma ação necessária.");
            return;
        }
        if (aiStudioProperties.apiKey() == null || aiStudioProperties.apiKey().isBlank()) {
            log.warn("API Key do Google AI Studio não configurada. O seeder não será executado.");
            return;
        }

        log.info("Banco de dados vazio. Solicitando dados estruturados ao Google AI Studio...");

        String prompt = "Gere uma lista de exatamente 80 livros, baseado no json de resposta que esta configurado. A 70% dos livros devem ser reais, com informações de livros famosos. Os outros 30%, gere livros fictícios criativos com autores também fictícios. Todos os livros devem ter as informações em português do Brasil.";

        try {
            Map<String, Object> requestBody = buildRequestBody(prompt);
            List<BookDocument> booksToSeed = webClient.post()
                    .uri(aiStudioProperties.apiUrl() + "?key=" + aiStudioProperties.apiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class) // Recebe a resposta diretamente como um JsonNode
                    .map(this::parseApiResponse)
                    .block();

            if (booksToSeed != null && !booksToSeed.isEmpty()) {
                bookRepository.saveAll(booksToSeed);
                log.info("{} livros gerados pela IA e inseridos com sucesso.", booksToSeed.size());
            }
        } catch (Exception e) {
            log.error("Falha ao se comunicar com a API do Google AI Studio ou processar a resposta.", e);
        }
    }

    private Map<String, Object> buildRequestBody(String prompt) throws IOException {
        // Passo 1: Obter o schema diretamente da classe de propriedades
        InputStream schemaInputStream = aiStudioProperties.requestSchema().getInputStream();
        Map<String, Object> responseSchema = objectMapper.readValue(schemaInputStream, new TypeReference<>() {});

        Map<String, Object> generationConfig = Map.of(
                "responseMimeType", "application/json",
                "responseSchema", responseSchema
        );

        return Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", generationConfig
        );
    }

    private List<BookDocument> parseApiResponse(JsonNode rootNode) {
        try {
            // Passo 1: Navegar até o nó que contém a string JSON
            JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");

            if (textNode.isMissingNode()) {
                log.error("Nó 'text' não encontrado na resposta da API: {}", rootNode.toPrettyString());
                return Collections.emptyList();
            }

            // Passo 2: Extrair a string JSON de dentro do nó de texto
            String jsonString = textNode.asText();

            // Passo 3: Fazer o parse da string extraída para um novo JsonNode
            JsonNode booksRoot = objectMapper.readTree(jsonString);
            JsonNode booksNode = booksRoot.get("books");

            if (booksNode == null || !booksNode.isArray()) {
                log.error("Nó 'books' não encontrado ou não é um array no JSON extraído: {}", jsonString);
                return Collections.emptyList();
            }

            // Passo 4: Converter o nó final para a nossa lista de documentos
            return objectMapper.convertValue(booksNode, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Erro ao fazer o parse da resposta JSON da IA.", e);
            return Collections.emptyList();
        }
    }
}
