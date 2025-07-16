# Usa uma imagem base que já inclui o JDK (Java Development Kit) e o Maven.
FROM maven:3.9.6-eclipse-temurin-21-alpine

# Define o diretório de trabalho dentro do container.
WORKDIR /app

# Copia todo o conteúdo do seu projeto para o diretório de trabalho no container.
# Isso inclui o pom.xml e a pasta src.
COPY . .

# Compila o projeto Maven e empacota a aplicação em um JAR executável.
RUN mvn clean package -DskipTests

# Expõe a porta em que a aplicação Spring Boot será executada (padrão 8080).
EXPOSE 8080

# Define o comando que será executado quando o container iniciar.
# Ele executa o JAR da sua aplicação Spring Boot.
# O nome do JAR é assumido como 'catalogo-do-sabio-api-1.0.0-SNAPSHOT.jar' ou similar.
# Ajuste 'catalogo-do-sabio-api-1.0.0-SNAPSHOT.jar' para o nome real do seu JAR se for diferente.
ENTRYPOINT ["java", "-jar", "target/catalogo-do-sabio-api-1.0.0-SNAPSHOT.jar"]
