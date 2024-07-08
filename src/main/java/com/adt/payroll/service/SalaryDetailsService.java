package com.adt.payroll.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.adt.payroll.dto.SalaryDTO;
import com.adt.payroll.dto.SalaryDetailsDTO;


public interface SalaryDetailsService {

	public ResponseEntity<String> saveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO);
	public ResponseEntity<SalaryDetailsDTO> calculateAndSaveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO);	
	ResponseEntity<List<SalaryDTO>> getEmployeeSalaryById(Integer empId);

}
