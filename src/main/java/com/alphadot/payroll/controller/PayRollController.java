package com.alphadot.payroll.controller;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alphadot.payroll.model.PaySlip;
import com.alphadot.payroll.service.PayRollService;
//import com.hrms.payrollgeneration.model.Employee;

@RestController
public class PayRollController {

	@Autowired
	private PayRollService payRollService;

	@GetMapping("/slip")
	public ResponseEntity<PaySlip> payrollCreate(@RequestParam("empId") int empId, @RequestParam("month") String month) throws FileNotFoundException, MalformedURLException {
		return ResponseEntity.ok(payRollService.createPaySlip(empId,month));
	}
	
	
	
	
  
}
