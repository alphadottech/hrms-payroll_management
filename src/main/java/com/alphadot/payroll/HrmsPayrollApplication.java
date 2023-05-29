package com.alphadot.payroll;

import com.alphadot.payroll.service.TableDataExtractor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication

public class HrmsPayrollApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context= SpringApplication.run(HrmsPayrollApplication.class, args);

		TableDataExtractor dataExtractor = context.getBean(TableDataExtractor.class);
	}

}
