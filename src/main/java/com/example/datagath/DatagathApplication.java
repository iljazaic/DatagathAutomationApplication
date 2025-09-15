package com.example.datagath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling  // THIS IS REQUIRED!
@SpringBootApplication
public class DatagathApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatagathApplication.class, args);
	}

}
