# O Catálogo do Sábio API

Uma API REST simples e escalável para uma livraria independente, permitindo a navegação por livros, busca por gênero e autor, e rastreamento de itens visualizados recentemente.

---

## Sumário

1.  [Como Executar o Projeto](#1-como-executar-o-projeto)
    * [1.1. Pré-requisitos](#11-pré-requisitos)
    * [1.2. Configuração da API Key do Google AI Studio](#12-Configuração-da-API-Key-do-Google-AI-Studio-para-geração-dos-livros)
    * [1.3. Execução das Dependências (MongoDB e Redis via Docker Compose)](#13-Execução-das-Dependências)
    * [1.4. Execução da Aplicação](#14-execução-da-aplicação)
    * [1.5. Acessando a API](#15-acessando-a-api)
2.  [Visão Geral do Desafio](#2-visão-geral-do-desafio)
3.  [Arquitetura da Solução](#3-arquitetura-da-solução)
    * [3.1. Arquitetura Lógica (Clean Architecture / Hexagonal)](#31-arquitetura-lógica-clean-architecture--hexagonal)
    * [3.2. Arquitetura Técnica](#32-arquitetura-técnica)
4.  [Decisões de Design e Implementação](#4-decisões-de-design-e-implementação)
    * [4.1. Autenticação e Segurança (OAuth2 / Keycloak)](#41-autenticação-e-segurança-oauth2--keycloak)
    * [4.2. Estrutura de Dados (MongoDB)](#42-estrutura-de-dados-mongodb)
    * [4.3. Mecanismo de Cache (Redis)](#43-mecanismo-de-cache-redis)
    * [4.4. Funcionalidade "Visualizados Recentemente"](#44-funcionalidade-visualizados-recentemente)
    * [4.5. Aquisição e Geração de Dados (Google AI Studio / Seeder)](#45-aquisição-e-geração-de-dados-google-ai-studio--seeder)
    * [4.6. API REST (Endpoints)](#46-api-rest-endpoints)
    * [4.7. Tratamento de Erros](#47-tratamento-de-erros)
    * [4.8. Qualidade do Código](#48-qualidade-do-código)
5.  [Testes](#5-testes)
    * [5.1. Testes Unitários](#51-testes-unitários)
    * [5.2. Testes de Integração](#52-testes-de-integração)
6.  [Melhorias e Considerações Finais](#6-melhorias-e-considerações-finais)
    * [6.1. Possíveis Melhorias Futuras](#61-possíveis-melhorias-futuras)
    * [6.2. Desafios Encontrados](#62-desafios-encontrados)
7.  [Autor](#7-autor)

---

## 1. Como Executar o Projeto

### 1.1. Pré-requisitos

* **Docker Desktop:** Necessário para rodar o MongoDB, Redis e Keycloak em containers.
* **Docker Compose:** Incluído no Docker Desktop.
* **Maven:** Para construir e rodar a aplicação Java.
* **JDK 21:** Para compilar e executar a aplicação.

### 1.2. Configuração da API Key do Google AI Studio para geração dos livros

A api utiliza a api do AI Studio (Gemini) para gerar os dados dos livros. Para que o seeder de dados funcione, você deve configurar sua chave de API do Google AI Studio.

1.  Obtenha sua chave de API em [Google AI Studio](https://aistudio.google.com/app/apikey).
2.  Adicione uma variavel de ambiente na sua configuração de run da ide: "GOOGLE_API_KEY", com o valor da sua chave, ou, no arquivo `application.yaml` em `src/main/resources`, adicione ou atualize a seguinte propriedade:

    ```yaml
    google:
      aistudio:
        api-key: ${GOOGLE_API_KEY}
    ```
    Alternativamente, você pode adicionar esta chave como variavel de ambiente do seu sistema operacional/container, ou passar esta chave como uma variável de ambiente via linha de comando, se escolher esta opção para rodar a api.

### 1.3. Execução das Dependências

A própria aplicação, através da dependência spring-boot-docker-compose gerencia o docker compose e roda o comando para subir os containers automaticamente, junto com a aplicação em sí.
Assim como derruba os containers quando a aplicação é derrubada. Então siga direto para o passo 1.4

Se desejar subir os containers de forma independente da aplicação, siga os passos abaixo:

1.  Abra o terminal na raiz do projeto (onde está o `docker-compose.yml`).
2.  Execute o comando para iniciar os serviços de MongoDB e Redis:

    ```bash
    docker-compose up -d
    ```
    * O `-d` faz com que os containers rodem em segundo plano.
    * A primeira execução pode levar alguns minutos, pois o Docker fará o download das imagens (mongo, redis).

3.  Para parar os serviços, execute:

    ```bash
    docker-compose down
    ```

PS: Não esqueça de se certificar que o docker desktop esta rodando :).

### 1.4. Execução da Aplicação

A aplicação utiliza `spring-boot-docker-compose` para gerenciar todo o ambiente (API, MongoDB, Redis, Keycloak) com um único comando.
Você pode iniciar a aplicação Java:

1.  **Via Maven (Terminal):**
    Abra um novo terminal na raiz do projeto e execute:

    Para terminal Unix:
    ```bash
    GOOGLE_API_KEY="SUA_CHAVE_DE_API_AQUI" mvn spring-boot:run
    ```
    Para terminal Windows:
    ```bash
    mvn spring-boot:run -DGOOGLE_API_KEY="SUA_CHAVE_DE_API_AQUI"
    ```
    * O `MongoDatabaseCharger` populará o MongoDB com 80 livros se o banco estiver vazio.

### 1.5. Acessando a API

#### 1.5.1. Documentação Interativa (Swagger UI)

Após a execução bem-sucedida da sua aplicação Java, o swagger da API estará disponível em `http://localhost:8080/catalogo-do-sabio/swagger-ui/index.html`.
`http://localhost:8080/catalogo-do-sabio/swagger-ui/index.html`

#### 1.5.2. Testando com Postman

Uma collection do Postman está disponível na pasta `/collection` na raiz do projeto. Ela contém todos os endpoints pré-configurados.

1.  **Importe a collection** para o seu Postman.
2.  **Obtenha o Token de Acesso:** A collection inclui uma requisição "Get Access Token" que obtém um token JWT do Keycloak. Execute-a primeiro.
3.  **Use o Token:** Configure o token recebido como a variável `access_token` no Postman para autorizar as suas requisições aos endpoints protegidos.

#### 1.5.3. Exemplos de Chamadas (usando `curl`)

**1. Obter o Token de Acesso:**
```bash
curl --location 'http://localhost:8180/realms/sabio-realm/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=catalogo-client' \
--data-urlencode 'client_secret=AIzaSyDzMSM-0jmUGtkw8KDoQYuEk95EmIOGK_Q' \
--data-urlencode 'username=leitor' \
--data-urlencode 'password=password' \
--data-urlencode 'grant_type=password'
```
Copie o valor do `access_token` da resposta.

**2. Fazer uma Chamada Autenticada:**
Substitua `<SEU_TOKEN_AQUI>` pelo token obtido.
```bash
curl -X GET -H "Authorization: Bearer <SEU_TOKEN_AQUI>" "http://localhost:8080/catalogo-do-sabio/v1/books"
```

Também é possível utilizar o token gerado no swagger através do botão Authorize. 

---

## 2. Visão Geral do Desafio

Este projeto consiste em desenvolver uma api para um sistema de uma livraria, capaz de consultar livros de uma base de dados, que seria populada também para fim de teste pela api. Apesar de ter um escopo supostamente simples, a liberdade criativa foi um ponto crucial na definição de como ela seria implementada.
Minha abordagem consistiu em primeiro analisar os requisitos e definir as tecnologias. Qual banco de dados usar? Como extrair as informações dos livros? Qual tipo de arquitetura de sistema?
A dosagem de o que é interessante e o que seria um exagero (overengineering) não é tão clara, mas os requisitos citando justamente a liberdade criativa me incentivaram a ousar sem fugir do escopo, e ainda sim desenvolver um sistema escalavel, com boas tecnologias e bons padrões de desenvolvimento.

**Funcionalidades Implementadas:**

* **Listagem de Livros:** Recupera todos os livros disponíveis no catálogo.
* **Busca por ID:** Retorna os detalhes de um livro específico.
* **Busca por Gênero:** Filtra livros por um gênero específico.
* **Busca por Autor:** Filtra livros por um autor específico.
* **Livros Visualizados Recentemente:** Mantém um registro dos últimos livros consultados por um cliente/sessão.
* **Segurança:** Todos os endpoints são protegidos via OAuth2.

---

## 3. Arquitetura da Solução

### 3.1. Arquitetura Lógica (Clean Architecture / Hexagonal)

A aplicação foi desenvolvida seguindo os princípios da **Clean Architecture (ou Arquitetura Hexagonal)**, promovendo a separação de preocupações e a inversão de dependências. Isso garante que o domínio da aplicação seja independente de frameworks, bancos de dados ou interfaces de usuário.

**Camadas Principais:**

* **`core.domain`**: Contém as entidades de negócio (`BookEntity`) e exceções de domínio (`BookNotFoundException`). É a camada mais interna e não possui dependências de outras camadas da aplicação.
* **`core.usecase`**: Define as regras de negócio da aplicação. Contém as interfaces de entrada (`boundary.in`) para os casos de uso (ex: `BookUseCase`, `RecentlyViewedUseCase`) e as interfaces de saída (`boundary.out`) para as portas de adaptação (ex: `BookRepositoryPort`, `RecentlyViewedPort`). As implementações (`BookUseCaseImpl`, `RecentlyViewedUseCaseImpl`) residem aqui, dependendo apenas das interfaces de portas.
* **`infrastructure`**: Esta é a camada de adaptação. Contém as implementações das portas de saída definidas no `core.usecase.boundary.out`, além das configurações de frameworks e integrações externas.
    * **`infrastructure.web.controller`**: Adapta as requisições HTTP para chamadas aos casos de uso e mapeia as entidades de domínio para modelos da API.
    * **`infrastructure.web.mapper`**: Mapeia entre entidades de domínio e modelos da API (gerados via OpenAPI).
    * **`infrastructure.persistence.mongodb`**: Contém os documentos do MongoDB (`BookDocument`), o repositório Spring Data (`SpringDataBookMongoRepository`) e o adaptador (`MongoBookRepositoryAdapter`) que implementa `BookRepositoryPort`.
    * **`infrastructure.persistence.redis`**: Contém o adaptador (`RedisRecentlyViewedAdapter`) que implementa `RecentlyViewedPort` para interação com o Redis.
    * **`infrastructure.configuration`**: Configurações de beans do Spring (ex: `WebClient`, `BookUseCase`, `RecentlyViewedUseCase`).
    * **`infrastructure.databasecharger`**: Componente responsável pela carga inicial de dados.

**Estrutura de Pacotes:**
```
└── br.com.livraria.catalogodosabioapi
    ├── core
    │   ├── domain
    │   │   ├── exception
    │   │   │   └── BookNotFoundException.java
    │   │   └── BookEntity.java
    │   └── usecase
    │       ├── boundary
    │       │   ├── in
    │       │   │   └── BookUseCase.java
    │       │   │   └── RecentlyViewedUseCase.java
    │       │   └── out
    │       │       └── BookRepositoryPort.java
    │       │       └── RecentlyViewedPort.java
    │       ├── BookUseCaseImpl.java
    │       └── RecentlyViewedUseCaseImpl.java
    │
    └── infrastructure
        ├── configuration
        │   ├── AiStudioProperties.java
        │   └── BeanConfiguration.java
        ├── databasecharger
        │   └── MongoDatabaseCharger.java
        ├── persistence
        │   ├── mongodb
        │   │   ├── document
        │   │   │   └── BookDocument.java
        │   │   ├── mapper
        │   │   │   └── BookDocumentMapper.java
        │   │   └── repository
        │   │       ├── MongoBookRepositoryAdapter.java
        │   │       └── SpringDataBookMongoRepository.java
        │   └── redis
        │       └── RedisRecentlyViewedAdapter.java
        └── web
            ├── controller
            │   ├── BookController.java
            │   └── GlobalExceptionHandler.java
            └── mapper
                └── BookApiMapper.java
```

### 3.2. Arquitetura Técnica

A solução utiliza as seguintes tecnologias:

* **Linguagem de Programação:** Java 21
  Esta é a versão do java que balanceia entre a estabilidade e a modernidade, por isso foi escolhida.

* **Framework:** Spring Boot 3.3.1
  Mesmo cenário, uma das últimas versões estáveis do Spring. E com maior compatibilidade com as demais dependências

* **Segurança:** Spring Security, OAuth2 (Resource Server), Keycloak (Authorization Server)
  Autenticação em apis é muito importante, e foi um requisito que eu fiz questão de implementar. Mesmo sem experiencia nisso, através de bastante pesquisa identifiquei qeu a forma mais simples de implementar isso seria utilizando o keycloack, com a possibilidade de criar o Resource Server ja pré configurado, mantendo a api reprodutível em outros ambientes.

* **Banco de Dados:** MongoDB (para armazenamento persistente de livros)
  A princípio, a questão era: trabalhar com um banco de dados não relacional ou um relacional?
  A principal vantagem de um banco de dados relacional, que é sua integridade e consistência nos dados não era uma realidade necessária nese projeto. O requisito é uma api eficiente, que só teria consultas de rápido acesso e pouquissimas ações de atualização ou inserção. Não tinha mais de uma entidade complexa que precisaria se relacionar com outras, então a aplicação do modelo relacional, se corretamente dentro das Formas Normais, causaria uma complexidade desnecessária.
  Em comparação, um não relacional oferece um schema flexível, alta disponibilidade e escalabilidade horizontal e recuperação eficiente para consultas de catálogo, pois todas as informações, como autor e genero, estão armazenadas no mesmo documento, sem necessidades de joins.

* **Cache e Estruturas de Dados:** Redis (para cache de consultas e a funcionalidade "Visualizados Recentemente")
  O uso do Cache ajuda a melhorar ainda mais a eficiência e diminuir os tempos de reposta. Escolher o redis para isso apresenta vantagens em relação a outras tecnologias de caching. O Redis suporta diversasr estruturas de dados, e ter uma lista em cache foi fundamental para a funcionalidade de Vistos recentemente. Logo a complexidade adicional que o redis trás é bem menor do que as vantagens do seu uso. Além de ser uma ferramenta muito popular e usada no mercado.

* **API REST:** Implementada com Spring WebFlux (utilizando `WebClient` para chamadas externas)
  Este é o Client recomendado pelo próprio spring framework, muito sofisticado, com suporte a requisições assincronas e facilmente utilizavel por ser gerenciado e acoplado ao contexto do spring.

* **Mapeamento de Objetos:** MapStruct (para mapear entre entidades de domínio, documentos de persistência e modelos da API)
  Uma biblioteca consolidada para simplificar a transferência de dados entre camadas do serviço, pois com facilidade gera os mapeamentos necessários.

* **Documentação da API:** OpenAPI 3.0.3
  A utilização do conceito de Contract First para desenvolvimento de apis, além da facilidade de gerar o swagger de forma desacoplada do código java em sí, com o uso do openapi-generator, é possível gerar a camada que recebe as requisições rest em tempo de build, o que trás muita facilidade para desenvolver a partir disso.

* **Geração de Dados:** Google AI Studio (API Gemini 2.0 Flash) para popular o banco de dados.
  Utilizar uma ia para gerar os dados não seria uma ideia que eu teria naturalmente, mas por ter essa possibilidade descrita no desafio e já ser uma coisa que eu gosto de aprender sobre e já havia, inclusive, feito um projeto pessoal utilizando o AI Studio, resolvi aproveitar para trazer um toque de criatividade para a api, juntanto com um asunto que está muito em alta.

* **Testes:** JUnit 5, Mockito, Spring Boot Test, Testcontainers (para MongoDB e Redis em testes de integração).
  Os testes, para garantir a qualidade e o funcionamento unitário do código é muito importante, e além das tecnologias já padrão do java, o testcontainers, para fazer os testes de integração facilitam muito.

* **Containerização:** Docker e Docker Compose.
  A forma mais facil e eficiente de configurar containers para subir localmente e integrar com apis durante os testes

---

## 4. Decisões de Design e Implementação

### 4.1. Autenticação e Segurança (OAuth2 / Keycloak)

* A API é protegida utilizando o padrão **OAuth2** e atua como um **Resource Server**.
* A autenticação de utilizadores e a emissão de tokens JWT são delegadas a um **Authorization Server** externo, implementado com **Keycloak** rodando em um container Docker.
* A configuração do Keycloak (realm, cliente e utilizador de teste) é **totalmente automatizada** através de um ficheiro de importação (`realm-config.json`), garantindo um ambiente 100% reprodutível.
* O Spring Security (`spring-boot-starter-oauth2-resource-server`) é configurado para validar os tokens JWT emitidos pelo Keycloak, verificando a sua assinatura e expiração com base no `issuer-uri` definido.

### 4.2. Estrutura de Dados (MongoDB)

* **`BookDocument`**: Representa a estrutura de um livro no MongoDB. Os campos são mapeados diretamente para as propriedades do documento JSON no banco de dados.
* **`BookEntity`**: É a representação do livro na camada de domínio, agnóstica à persistência.
* **`BookDocumentMapper`**: Responsável por converter entre `BookEntity` e `BookDocument`, garantindo que a camada de domínio não tenha conhecimento da implementação do banco de dados.
* **Índices:** Os índices nos campos `genres` e `authors` foram criados para acelerar as operações de busca. A configuração de **`collation`** nesses índices é crucial: ela define as regras de comparação de strings (como sensibilidade a maiúsculas/minúsculas ou acentos) e garante que o índice seja eficientemente utilizado, otimizando o desempenho das consultas textuais.

### 4.3. Mecanismo de Cache (Redis)

* O Redis é utilizado como um cache distribuído para otimizar as consultas frequentes.
* As anotações `@Cacheable` do Spring Cache são aplicadas nos métodos `findAll`, `findById`, `findByGenre`, `findByAuthor` e `findAllByIds` do `MongoBookRepositoryAdapter`. Isso garante que, após a primeira consulta ao MongoDB, os resultados sejam armazenados no Redis, e chamadas subsequentes para os mesmos parâmetros recuperem os dados diretamente do cache, reduzindo a carga no banco de dados.
* **Expiração de Cache (TTL):** Foram definidos tempos de expiração (Time To Live) diferentes para cada tipo de cache através de um bean `RedisCacheManagerBuilderCustomizer`. Caches de itens individuais (`book`, `booksByIds`) possuem um TTL maior (1 hora), enquanto caches de listagens (`books`, `booksByGenre`) possuem um TTL menor (10 minutos) para refletir novas adições ao catálogo mais rapidamente.

### 4.4. Funcionalidade "Visualizados Recentemente"

* Implementada utilizando uma lista no Redis para cada cliente (`X-Client-ID`).
* Quando um livro é consultado via `GET /books/{id}`, o ID do livro é adicionado à lista de visualizados recentemente do cliente no Redis.
* A operação de salvamento no Redis é **síncrona** no `BookController`. A decisão de manter síncrona foi baseada na premissa de que a operação de cache no Redis é extremamente rápida e não impactaria significativamente o tempo de resposta da API para a busca principal.
* A lista é limitada a um número máximo de itens (`MAX_ITEMS = 10`) para evitar o crescimento excessivo e manter apenas os itens mais relevantes.
* **Expiração da Lista:** A lista de visualizados de cada utilizador possui um TTL (Time To Live) de **5 dias**. Sempre que um novo livro é adicionado, o tempo de vida da lista é renovado, garantindo que ela só expire após 5 dias de inatividade do utilizador.

### 4.5. Aquisição e Geração de Dados (Google AI Studio / Seeder)

* A aplicação inclui um `CommandLineRunner` (`MongoDatabaseCharger`) que é executado no perfil `dev` (`spring.profiles.active=dev`).
* Este seeder verifica se o banco de dados MongoDB está vazio. Se estiver, ele faz uma chamada à API do Google AI Studio (Gemini 2.0 Flash) para gerar uma lista de 80 livros (70% reais, 30% fictícios).
* A integração com a API de IA é feita via `WebClient` e `ObjectMapper` para construir a requisição com o `responseSchema` JSON e parsear a resposta.
* A configuração para a chaamda foi feita baseado no modelo que o próprio AI Studio fornece quando utilizado através da sua plataforma.
* **Importante:** A chave da API do Google AI Studio deve ser configurada via variável de ambiente `GOOGLE_API_KEY`.


### 4.6. API REST (Endpoints)

A API expõe os endpoints sob o prefixo `/catalogo-do-sabio/v1`. Todos os endpoints, exceto os de documentação, requerem um token de autenticação JWT válido.
A documentação sobre os endpoints pode ser visualizada no swagger da aplicação.
Na raiz do projeto, na pasta "collection/", há uma collection do postman para consumo com todos os endpoints + o de autenticação. 

### 4.7. Tratamento de Erros

* Um `GlobalExceptionHandler` (`@RestControllerAdvice`) é implementado para centralizar o tratamento de exceções.
* **`BookNotFoundException`**: Mapeada para `HTTP 404 Not Found`, com uma mensagem informativa e timestamp.
* **`Exception.class` (genérica)**: Mapeada para `HTTP 500 Internal Server Error`, com uma mensagem genérica de erro inesperado para o cliente e log detalhado no servidor.

### 4.8. Qualidade do Código

* **Estrutura de Pacotes:** Organizada para refletir a Clean Architecture.
* **Lombok:** Reduz o boilerplate de código (getters, setters, construtores).
* **Logging:** Utilização de `slf4j` para logs informativos e de debug.
* **Testes Unitários:** Cobertura significativa de todas as classes (Exceto POJOs/DTOs).
* **Testes de Integração:** Validação dos fluxos completos da API com dependências reais (MongoDB, Redis) via Testcontainers.

---

## 5. Testes

### 5.1. Testes Unitários

* **Ferramentas:** JUnit 5 e Mockito.
* **Cobertura:** Focam em testar unidades isoladas de código (classes de domínio, casos de uso, adaptadores) mockando suas dependências.
* **AAA.** Os testes seguiram a estrutura Arrange, Act & Assert para que os testes sigam uma estrutura baseada em um padrão consolidado.

### 5.2. Testes de Integração

* **Ferramentas:** JUnit 5, Spring Boot Test, Testcontainers (para MongoDB e Redis).
* **Cobertura:** Validam o fluxo completo da API, incluindo a interação com o MongoDB e Redis. Os Testcontainers garantem um ambiente de banco de dados e cache limpo e isolado para cada execução de teste.
* **Cenários Cobertos:**
    * Busca de livros por ID, gênero e autor.
    * Comportamento do cache.
    * Funcionalidade de "Visualizados Recentemente" (salvamento e recuperação).
    * Tratamento de erros.

---

## 6. Melhorias e Considerações Finais

### 6.1. Possíveis Melhorias Futuras

* **Paginação Avançada:** Implementar paginação mais robusta para o endpoint `/books` (e talvez outros), permitindo `page`, `size` e `sort` parâmetros.
* **<s>Autenticação e Autorização:** Adicionar um mecanismo de segurança (ex: JWT) para proteger os endpoints da API.</s>
* **Métricas e Monitoramento:** Integrar com ferramentas de monitoramento (ex: Dynatrace, Grafana) para observar a performance da API.
* **Testes automatizados:** Desenvolvimento de testes automatizados com cucumber para cobrir diversos cenários.
* **Implementação Assíncrona para "Visualizados Recentemente":** Se a operação de salvamento no Redis se tornar um gargalo de performance em cenários de alto volume, reavaliar a implementação assíncrona (ex: com eventos Spring ou filas de mensagens).
* **Adapter gRPC:** Adicionar uma interface gRPC que reutilize os mesmos casos de uso do core, demonstrando a flexibilidade da arquitetura.
* **Pipeline de CI/CD:** Configurar um workflow no GitHub Actions (ou outra ferramenta de CI/CD) para automatizar a build, testes e deploy da aplicação a cada push.

### 6.2. Desafios Encontrados

* **[Tomada de decisão]** Os maiores desafios foram nas tomadas de decisão. Entender as possibilidades e escolher dentre elas, seja as tecnologias ou a arquitetura. No fim sempre sabemos que há uma forma melhor de fazer, mesmo assim a decisão tem que ser tomada. XD
* **[Integração com api do Google]** Após decidir utilizar a api do AI Studio do Google para gerar os livros, também por ter já utilizado isso em outro projeto pessoal (Porém em python e bem menos sério), eu imaginei que seria mais simples, mas a complexidade do código chama essa api é alta, e mesmo com ajuda do proprio Gemini, não consegui melhorar mais do que como está agora.

---

## 7. Autor

* **[Vinicius Cardoso / https://github.com/viniciuscsena / https://www.linkedin.com/in/vinicsena/]**
