package com.khabalita.sistemacomercial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SistemacomercialApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemacomercialApplication.class, args);
	}

}
