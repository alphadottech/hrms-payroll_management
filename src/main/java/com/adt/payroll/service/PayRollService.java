package com.adt.payroll.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import org.springframework.web.multipart.MultipartFile;

import com.adt.payroll.dto.SalaryDetailsDTO;
import com.adt.payroll.model.PaySlip;
import com.adt.payroll.model.SalaryModel;

public interface PayRollService {
	public PaySlip createPaySlip(int empId, String month, String year) throws ParseException, IOException;

	public String generatePaySlip(MultipartFile file) throws IOException, ParseException;

	public byte[] viewPay(SalaryModel salaryModel, String month, String year)
			throws ParseException, UnsupportedEncodingException;

	String updateNetAmountInExcel(MultipartFile file) throws IOException;

	public String generatePaySlipForAllEmployees() throws ParseException, IOException;

	public SalaryDetailsDTO getEmployeePayrollDetailsById(Integer empId);

}
