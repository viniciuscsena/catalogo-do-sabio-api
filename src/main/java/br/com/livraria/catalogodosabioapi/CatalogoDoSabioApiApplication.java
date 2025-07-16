package br.com.livraria.catalogodosabioapi;

import br.com.livraria.catalogodosabioapi.infrastructure.configuration.AiStudioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties(AiStudioProperties.class)
@EnableCaching
@EnableAsync
public class CatalogoDoSabioApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogoDoSabioApiApplication.class, args);
	}

}
