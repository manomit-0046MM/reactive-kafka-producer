package com.bsolz.reactivekafkaproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReactiveKafkaProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveKafkaProducerApplication.class, args);
	}

}
