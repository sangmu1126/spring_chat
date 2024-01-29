package com.inha.everytown;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EverytownApplication {

	public static void main(String[] args) {
		SpringApplication.run(EverytownApplication.class, args);
	}

}
