package com.adt.payroll.service;

import com.adt.payroll.model.AppraisalDetails;
import org.springframework.http.ResponseEntity;

import com.adt.payroll.dto.SalaryDetailsDTO;


public interface SalaryDetailsService {

	public ResponseEntity<String> saveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO);
	public ResponseEntity<SalaryDetailsDTO> calculateAndSaveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO);
	public ResponseEntity<String> addAppraisalDetails(AppraisalDetails appraisalDetails);

}
