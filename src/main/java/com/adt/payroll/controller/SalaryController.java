package com.adt.payroll.controller;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adt.payroll.model.SalaryModel;
import com.adt.payroll.service.SalaryService;

@RestController
@RequestMapping("/salary")
public class SalaryController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SalaryService salaryService;

	@PreAuthorize("@auth.allow('ROLE_ADMIN')")
	@GetMapping("/getAllEmpSalary")
	public ResponseEntity<List<SalaryModel>> getAllEmpSalary() throws ParseException {
		LOGGER.info("Payroll service: salary:  getAllEmpSalary() Info level log msg");
		return new ResponseEntity<>(salaryService.getAllEmpSalary(), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('ROLE_USER',T(java.util.Map).of('currentUser', #empId))")
	@GetMapping("/getSalaryById/{empId}")
	public ResponseEntity<Optional<SalaryModel>> getSalaryById(@PathVariable("empId") int empId) {
		LOGGER.info("Payroll service: salary:  getSalaryById Info level log msg");
		return new ResponseEntity<>(salaryService.getSalaryById(empId), HttpStatus.OK);
	}
}
