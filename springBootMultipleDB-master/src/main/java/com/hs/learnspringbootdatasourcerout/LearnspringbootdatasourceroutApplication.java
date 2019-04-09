package com.hs.learnspringbootdatasourcerout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class LearnspringbootdatasourceroutApplication {



	public static void main(String[] args) {
		SpringApplication.run(LearnspringbootdatasourceroutApplication.class, args);
	}
}
