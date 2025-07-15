package br.com.livraria.catalogodosabioapi;

import br.com.livraria.catalogodosabioapi.infrastructure.configuration.AiStudioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiStudioProperties.class)
public class CatalogoDoSabioApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogoDoSabioApiApplication.class, args);
	}

}
