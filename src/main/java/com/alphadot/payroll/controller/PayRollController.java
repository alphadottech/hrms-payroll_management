package com.alphadot.payroll.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alphadot.payroll.model.LeaveTime;
import com.alphadot.payroll.service.PayRollService;
//import com.hrms.payrollgeneration.model.Employee;

import java.text.ParseException;
import java.util.List;

@RestController
public class PayRollController {

	@Autowired
	private PayRollService payRollService;

	@PostMapping("/payroll")
	public ResponseEntity<String> payrollCreate(@RequestParam("empId") int empId, @RequestParam("month") String month) throws FileNotFoundException, MalformedURLException {
		return ResponseEntity.ok(payRollService.createPaySlip(empId,month));
	}
	
	@PostMapping("/leaveSave/{empId}")
	public ResponseEntity<String> saveLeave(@PathVariable("empId") int empId, @RequestBody List<String> list){
		return ResponseEntity.ok(payRollService.saveLeave(empId,list));
		
	}
	
	
  
}
