package com.alphadot.payroll.controller;

import java.text.ParseException;
import java.util.List;

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

	@GetMapping("/getAllEmpSalary")
	public ResponseEntity<List<SalaryModel>> getAllEmpSalary() throws ParseException{
		return new ResponseEntity<>(salaryService.getAllEmpSalary(),HttpStatus.OK);
	}
	
	@GetMapping("/getSalaryById/{empId}")
	public ResponseEntity<String> getEmpLeaves(@PathVariable("empId") int empId) {
		return new ResponseEntity<>(salaryService.workingDays(empId), HttpStatus.OK);
	}
}
