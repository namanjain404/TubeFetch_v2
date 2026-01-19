package com.NamanJain.TubeFetch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TubeFetchApplication {

	public static void main(String[] args) {
		SpringApplication.run(TubeFetchApplication.class, args);
	}

}
