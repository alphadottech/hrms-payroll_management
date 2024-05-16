package com.adt.payroll.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Base64;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.adt.payroll.model.PaySlip;
import com.adt.payroll.model.SalaryModel;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.service.PayRollService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class PayRollController {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PayRollService payRollService;

    @Autowired
    private TimeSheetRepo timeSheetRepo;

    @PreAuthorize("@auth.allow('ROLE_ADMIN') or @auth.allow('ROLE_USER',T(java.util.Map).of('currentUser', #empId))")
    @GetMapping("/slip")
    public ResponseEntity<PaySlip> payrollCreate(@RequestParam("empId") int empId, @RequestParam("month") String month, @RequestParam("year") String year, HttpServletRequest request) throws ParseException, IOException, InvalidFormatException {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return ResponseEntity.ok(payRollService.createPaySlip(empId, month, year));
    }
    @PreAuthorize("@auth.allow('ROLE_ADMIN')")
    @PostMapping("/genPayAll")
    public ResponseEntity<Object> generatePaySlip(@RequestParam("file") MultipartFile file,HttpServletRequest request) throws IOException, ParseException {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return  new ResponseEntity<>(payRollService.generatePaySlip(file), HttpStatus.OK);
    }
    @PreAuthorize("@auth.allow('ROLE_ADMIN')")
    @PostMapping("/viewPay")
    public ResponseEntity<Object> viewPay(HttpServletRequest request, HttpServletResponse response,@RequestBody SalaryModel salaryModel, @RequestParam("month") String month , @RequestParam("year") String year) throws ParseException, IOException {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        response.setContentType("application/pdf");
        byte[] payPdf= payRollService.viewPay(salaryModel,month,year);
        String base64String = Base64.getEncoder().encodeToString(payPdf);
        return  new ResponseEntity<>(base64String,HttpStatus.OK);
    }

    @PostMapping("/calculateNetAmtPayable")
    public ResponseEntity<String> updateNetAmountInExcel(@RequestParam("file") MultipartFile file,HttpServletRequest request) throws IOException {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return ResponseEntity.ok(payRollService.updateNetAmountInExcel(file));
    }

  //  @PreAuthorize("@auth.allow('ROLE_ADMIN')")
	@PostMapping("/generatePaySlipAndSalaryValidationForAll")
	public ResponseEntity<Object> generatePaySlipAndSalaryValidationForAll(HttpServletRequest request)
			throws IOException, ParseException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return new ResponseEntity<>(payRollService.generatePaySlipForAllEmployees(), HttpStatus.OK);
	}

}
