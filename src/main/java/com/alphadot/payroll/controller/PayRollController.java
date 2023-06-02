package com.alphadot.payroll.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alphadot.payroll.model.PaySlip;
import com.alphadot.payroll.service.PayRollService;

@RestController
public class PayRollController {

	private static final Logger LOGGER = LogManager.getLogger(PayRollController.class);

	@Autowired
	private PayRollService payRollService;

	@PreAuthorize("@auth.allow('ROLE_ADMIN')")
	@GetMapping("/slip")
	public ResponseEntity<PaySlip> payrollCreate(@RequestParam("empId") int empId, @RequestParam("month") String month,
			@RequestParam("year") String year, @RequestParam("adhoc") int adhoc, HttpServletRequest request)
			throws ParseException, IOException, InvalidFormatException, SQLException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return ResponseEntity.ok(payRollService.createPaySlip(empId, month, year, adhoc));
	}

	@PreAuthorize("@auth.allow('ROLE_ADMIN')")
	@GetMapping("/genPayAll")
	public String generatePaySlip(@RequestParam("file") MultipartFile file) throws IOException, ParseException {
		return payRollService.generatePaySlip(file);
	}

}
