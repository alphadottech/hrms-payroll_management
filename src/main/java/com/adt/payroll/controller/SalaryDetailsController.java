package com.adt.payroll.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.dto.SalaryDTO;
import com.adt.payroll.dto.SalaryDetailsDTO;
import com.adt.payroll.model.AppraisalDetails;
import com.adt.payroll.model.Reward;
import com.adt.payroll.service.AppraisalDetailsService;
import com.adt.payroll.service.MonthlySalaryService;
import com.adt.payroll.service.SalaryDetailsService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/salarydetails")
public class SalaryDetailsController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SalaryDetailsService salaryDetailsService;

	@Autowired
	private MonthlySalaryService monthlySalaryService;

	@PreAuthorize("@auth.allow('SAVE_EMPLOYEE_SALARY_DETAILS')")
	@PostMapping("/saveEmployeeSalaryDetails")
	public ResponseEntity<SalaryDetailsDTO> saveSalaryDetails(@RequestBody SalaryDetailsDTO salaryDetailsDTO) {
		LOGGER.info("PayrollService: SalaryDetailsController: Employee saveSalaryDetails Info level log msg");
		//	return salaryDetailsService.saveSalaryDetails(salaryDetailsDTO);
		return salaryDetailsService.calculateAndSaveSalaryDetails(salaryDetailsDTO);
	}

	@PreAuthorize("@auth.allow('GET_SALARY_BY_ID')")
	@GetMapping("/getSalaryDetailsById/{empId}")
	public ResponseEntity<List<SalaryDTO>> getEmployeeSalaryById(@PathVariable Integer empId) {
		LOGGER.info("PayrollService: SalaryDetailsController:Getting employee salary by id Info level log msg");
		return salaryDetailsService.getEmployeeSalaryById(empId);
	}
	
	@PreAuthorize("@auth.allow('GET_ALL_MONTHLY_SALARY_DETAILS')")
	@GetMapping("/getAllMonthlySalaryDetails")
	public ResponseEntity<?> getAllSalaryDetails(HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
	    LOGGER.info("PayrollService: SalaryDetailsController:Getting all Monthly Salary Details Info level log msg");
		return monthlySalaryService.getAllMonthlySalaryDetails();
	}

	@PreAuthorize("@auth.allow('GET_ALL_MONTHLY_SALARY_DETAILS_BY_ID_EXPORT_TO_EXCEL')")
	@GetMapping("/getAllMonthlySalaryDetailsExportToExcelById/{empId}")
	public ResponseEntity<Resource> getAllMonthlySalaryDetailsExportToExcel(@PathVariable("empId") Integer empId) throws IOException {

		String filename = "alphadot_Monthly_Salary_Details_data.xlsx";
		ByteArrayInputStream newdata = monthlySalaryService.getExcelData(empId);
		InputStreamResource file = new InputStreamResource(newdata);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(file);
	}
}






