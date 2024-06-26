package com.adt.payroll.controller;

import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.model.AppraisalDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adt.payroll.dto.SalaryDetailsDTO;
import com.adt.payroll.service.SalaryDetailsService;

import java.util.List;

@RestController
@RequestMapping("/salarydetails")
public class SalaryDetailsController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SalaryDetailsService salaryDetailsService;

	@PreAuthorize("@auth.allow('SAVE_EMPLOYEE_SALARY_DETAILS')")
	@PostMapping("/saveEmployeeSalaryDetails")
	public ResponseEntity<SalaryDetailsDTO> saveSalaryDetails(@RequestBody SalaryDetailsDTO salaryDetailsDTO) {
		LOGGER.info("PayrollService: SalaryDetailsController: Employee saveSalaryDetails Info level log msg");
		//	return salaryDetailsService.saveSalaryDetails(salaryDetailsDTO);
		return salaryDetailsService.calculateAndSaveSalaryDetails(salaryDetailsDTO);
	}

	@PreAuthorize("@auth.allow('SAVE_APPRAISAL_DETAILS')")
	@PostMapping("/addAppraisalDetails")
	public ResponseEntity<String> addAppraisalDetails(@RequestBody AppraisalDetails appraisalDetails) {
		LOGGER.info("PayrollService: SalaryDetailsController:Employee addAppraisalDetails Info level log msg");
		ResponseEntity<String> responseEntity = salaryDetailsService.addAppraisalDetails(appraisalDetails);
		return responseEntity;
	}

	@PreAuthorize("@auth.allow('GET_ALL_EMPLOYEE_APPRAISAL_DETAILS')")
	@GetMapping("/getAllEmployeesWithLatestAppraisal")
	public ResponseEntity<List<AppraisalDetailsDTO>> getEmployeesWithLatestAppraisal() {
		LOGGER.info("PayrollService: SalaryDetailsController: Getting all employees with latest appraisal details");
		return salaryDetailsService.getEmployeesWithLatestAppraisal();
	}
}






