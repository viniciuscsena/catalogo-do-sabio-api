package br.com.livraria.catalogodosabioapi;

import org.springframework.boot.SpringApplication;

public class TestCatalogoDoSabioApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(CatalogoDoSabioApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
