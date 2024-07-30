package com.adt.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.adt.payroll.service.TableDataExtractor;

@SpringBootApplication
@EnableScheduling
public class HrmsPayrollApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext context= SpringApplication.run(HrmsPayrollApplication.class, args);

		TableDataExtractor dataExtractor = context.getBean(TableDataExtractor.class);
	}

}
