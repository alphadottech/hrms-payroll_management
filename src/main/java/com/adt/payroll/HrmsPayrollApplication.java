package com.adt.payroll;

import com.adt.payroll.service.TableDataExtractor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class HrmsPayrollApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext context= SpringApplication.run(HrmsPayrollApplication.class, args);

		TableDataExtractor dataExtractor = context.getBean(TableDataExtractor.class);
	}

}
