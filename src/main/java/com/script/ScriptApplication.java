package com.script;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ScriptApplication implements ApplicationRunner {

	private final DueDate dueDate;

	public ScriptApplication(DueDate dueDate) {
		this.dueDate = dueDate;
	}

	public static void main(String[] args) {
		SpringApplication.run(ScriptApplication.class, args);
	}


	@Override
	public void run(ApplicationArguments args) throws Exception {
		dueDate.paymentInvoice();
	}
}
