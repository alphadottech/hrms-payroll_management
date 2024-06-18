package com.adt.payroll.service;

import org.springframework.http.ResponseEntity;

import com.adt.payroll.dto.SalaryDetailsDTO;

public interface SalaryDetailsService {

	public ResponseEntity<String> saveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO);
	public ResponseEntity<SalaryDetailsDTO> calculateAndSaveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO);

}
