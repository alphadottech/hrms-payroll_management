package com.adt.payroll.service;

import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.dto.SalaryDTO;
import com.adt.payroll.model.AppraisalDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.adt.payroll.dto.SalaryDetailsDTO;

import java.util.List;


public interface SalaryDetailsService {

	public ResponseEntity<String> saveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO);
	public ResponseEntity<SalaryDetailsDTO> calculateAndSaveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO);
	public ResponseEntity<String> addAppraisalDetails(AppraisalDetails appraisalDetails);
	ResponseEntity<Page<AppraisalDetailsDTO>> getEmployeesWithLatestAppraisal(int page, int size);
	ResponseEntity<List<SalaryDTO>> getEmployeeSalaryById(Integer empId);

}
