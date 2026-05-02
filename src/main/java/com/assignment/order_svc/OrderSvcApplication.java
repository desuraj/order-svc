package com.assignment.order_svc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderSvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderSvcApplication.class, args);
	}

}
