package com.adt.payroll.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adt.payroll.dto.SalaryDetailsDTO;
import com.adt.payroll.service.SalaryDetailsService;

@RestController
@RequestMapping("/salaryDetails")
public class SalaryDetailsController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SalaryDetailsService salaryDetailsService;

	@PostMapping("/saveSalaryDetails")
	public ResponseEntity<String> saveSalaryDetails(@RequestBody SalaryDetailsDTO salaryDetailsDTO) {
		LOGGER.info("PayrollService: SalaryDetailsController: Employee saveSalaryDetails Info level log msg");

		String response = salaryDetailsService.saveSalaryDetails(salaryDetailsDTO);
		if (response.equalsIgnoreCase("Success")) {
			LOGGER.info("PayrollService: SalaryDetailsController: Employee saveSalaryDetails Saved Successfully");
			return new ResponseEntity<>(
					"EmployeeSalaryDetails by EmpId:" + salaryDetailsDTO.getEmpId() + " is Saved Succesfully",
					HttpStatus.OK);
		} else if (response.equalsIgnoreCase("NotExist")) {
			LOGGER.info("PayrollService: SalaryDetailsController: Employee Not Exist : ");
			return new ResponseEntity<>(
					"EmployeeSalaryDetails by EmpId:" + salaryDetailsDTO.getEmpId() + " is Not Exist", HttpStatus.OK);
		} else {
			LOGGER.info("PayrollService: SalaryDetailsController: Employee saveSalaryDetails Not Saved : ");
			return new ResponseEntity<>(
					"EmployeeSalaryDetails by EmpId:" + salaryDetailsDTO.getEmpId() + " is Not Saved", HttpStatus.OK);
		}
	}

}
