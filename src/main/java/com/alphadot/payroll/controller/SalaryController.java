package com.alphadot.payroll.controller;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alphadot.payroll.model.SalaryModel;
import com.alphadot.payroll.service.SalaryService;

@RestController
@RequestMapping("/salary")
public class SalaryController {

	@Autowired
	private SalaryService salaryService;

	private static final Logger log=LogManager.getLogger(SalaryController.class);
	
	@GetMapping("/getAllEmpSalary")
	public ResponseEntity<List<SalaryModel>> getAllEmpSalary() throws ParseException{
		log.info("Payroll service: salary:  getAllEmpSalary() Info level log msg");
		return new ResponseEntity<>(salaryService.getAllEmpSalary(),HttpStatus.OK);
	}

	
	@GetMapping("/getSalaryById/{empId}")
	public ResponseEntity<Optional<SalaryModel>> getSalaryById(@PathVariable("empId") int empId) {
	
		log.info("Payroll service: salary:  getSalaryById Info level log msg");
		
		return new ResponseEntity<>(salaryService.getSalaryById(empId), HttpStatus.OK);
	}
}
