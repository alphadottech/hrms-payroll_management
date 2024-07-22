package com.adt.payroll.service;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.adt.payroll.dto.SalaryDTO;
import com.adt.payroll.dto.SalaryDetailsDTO;
import com.adt.payroll.dto.ViewPaySlipDto;
import com.adt.payroll.model.MonthlySalaryDetails;
import com.adt.payroll.model.PaySlip;
import com.itextpdf.text.DocumentException;

public interface PayRollService {
	public PaySlip createPaySlip(int empId, String month, String year) throws ParseException, IOException;

	public String generatePaySlip(MultipartFile file, String email) throws IOException, ParseException;

	public ViewPaySlipDto viewPay(int empId, String month, String year) throws Exception;

//	public byte[] viewPay(SalaryModel salaryModel, String month, String year)
//			throws ParseException, UnsupportedEncodingException;

	String updateNetAmountInExcel(MultipartFile file) throws IOException;

	public String generatePaySlipForAllEmployees(String emailInput) throws ParseException, IOException;

	public SalaryDetailsDTO getEmployeePayrollSalaryDetailsByEmpId(Integer empId);
	
	public ResponseEntity<Object> validateAmount(Integer empid, SalaryDTO dto) throws ParseException, IOException;
	
	public String regenerateEmployeePayslip(Integer empid, MonthlySalaryDetails dto) throws DocumentException, IOException;

}
