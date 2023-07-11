package com.adt.payroll.service;

import com.adt.payroll.model.PaySlip;
import com.adt.payroll.model.SalaryModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.text.ParseException;

public interface PayRollService {
    public PaySlip createPaySlip(int empId, String month, String year) throws ParseException, IOException;

//    public PaySlip createPaySlip(String empId) throws ParseException, IOException, SQLException;
    public String generatePaySlip(MultipartFile file) throws IOException, ParseException;

    public byte[] viewPay(SalaryModel salaryModel, String month, String year) throws ParseException, UnsupportedEncodingException;

    String updateNetAmountInExcel(MultipartFile file) throws IOException;
}
