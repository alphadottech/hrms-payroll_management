package com.adt.payroll.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.text.ParseException;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adt.payroll.model.SalaryModel;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.service.PayRollService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.adt.payroll.model.PaySlip;
import com.adt.payroll.service.PayRollServiceImpl;

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
    public String generatePaySlip(@RequestParam("file") MultipartFile file) throws IOException, ParseException {

        return payRollService.generatePaySlip(file);
    }
    @PreAuthorize("@auth.allow('ROLE_ADMIN')")
    @PostMapping("/viewPay")
    public String viewPay(HttpServletRequest request, HttpServletResponse response,@RequestBody SalaryModel salaryModel, @RequestParam("month") String month , @RequestParam("year") String year) throws ParseException, IOException {
        response.setContentType("application/pdf");
        byte[] payPdf= payRollService.viewPay(salaryModel,month,year);
        String base64String = Base64.getEncoder().encodeToString(payPdf);
        return base64String;

    }

    @PostMapping("/calculateNetAmtPayable")
    public String updateNetAmountInExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return payRollService.updateNetAmountInExcel(file);
    }



}
