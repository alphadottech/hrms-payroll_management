package com.alphadot.payroll.controller;

import java.io.IOException;
import java.text.ParseException;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alphadot.payroll.model.PaySlip;
import com.alphadot.payroll.service.PayRollService;



@RestController
public class PayRollController {

	  private static final Logger LOGGER = LogManager.getLogger(PayRollController.class);
	  
	@Autowired
	private PayRollService payRollService;

	

	@GetMapping("/slip")
	public ResponseEntity<PaySlip> payrollCreate(@RequestParam("empId") int empId, @RequestParam("month") String month, @RequestParam("year") String year, HttpServletRequest request) throws ParseException, IOException {
		  LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return ResponseEntity.ok(payRollService.createPaySlip(empId,month,year));
	}
	

	@GetMapping("/genPay")
	public void generatePaySlip() throws IOException {
		
		payRollService.generatePaySlip();
		
	
	}
	
  
}
