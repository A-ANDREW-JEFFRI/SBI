package com.example.dto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.example.controller" , "com.example.shell"})

public class DtoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DtoApplication.class, args);
	}

}
