package com.adt.payroll.controller;

import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.dto.MonthSalaryDTO;
import com.adt.payroll.dto.SalaryDTO;
import com.adt.payroll.model.AppraisalDetails;
import com.adt.payroll.model.MonthlySalaryDetails;
import com.adt.payroll.model.Reward;
import com.adt.payroll.service.AppraisalDetailsService;
import com.adt.payroll.service.MonthlySalaryService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.adt.payroll.dto.SalaryDetailsDTO;
import com.adt.payroll.service.SalaryDetailsService;

import java.util.List;

@RestController
@RequestMapping("/salarydetails")
public class SalaryDetailsController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SalaryDetailsService salaryDetailsService;

	@Autowired
	private AppraisalDetailsService appraisalDetailsService;
	@Autowired
	private MonthlySalaryService monthlySalaryService;

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
		return salaryDetailsService.addAppraisalDetails(appraisalDetails);
	}

	@PreAuthorize("@auth.allow('GET_ALL_EMPLOYEE_APPRAISAL_DETAILS')")
	@GetMapping("/getAllEmployeesWithLatestAppraisal")
	public ResponseEntity<List<AppraisalDetailsDTO>> getAllEmployeesWithLatestAppraisal() {
		LOGGER.info("PayrollService: SalaryDetailsController:Getting all employees appraisal details Info level log msg");
		return salaryDetailsService.getEmployeesWithLatestAppraisal();
	}

	@PreAuthorize("@auth.allow('GET_SALARY_BY_ID')")
	@GetMapping("/getSalaryDetailsById/{empId}")
	public ResponseEntity<List<SalaryDTO>> getEmployeeSalaryById(@PathVariable Integer empId) {
		LOGGER.info("PayrollService: SalaryDetailsController:Getting employee salary by id Info level log msg");
		return salaryDetailsService.getEmployeeSalaryById(empId);
	}
	@PreAuthorize("@auth.allow('GET_ALL_APPRAISAL_DETAILS_BY_ID')")
	@GetMapping("/getAllAppraisalDetailsbyId/{id}")
	public ResponseEntity<List<AppraisalDetails>> getAppraisalDetailsById(@PathVariable Integer id) {
		return appraisalDetailsService.getAppraisalDetails(id);
	}

	@PreAuthorize("@auth.allow('GET_REWARD_DETAILS_BY_ID')")
	@GetMapping("/getRewardDetails/{id}")
	public List<Reward> getRewardDetailByEmployeeId(@PathVariable Integer id) {
		return appraisalDetailsService.getRewardDetailsByEmployeeId(id);
	}
	@PreAuthorize("@auth.allow('SAVE_REWARD_DETAILS')")
	@PostMapping("/saveRewardDetails")
	public ResponseEntity<String>saveRewardDetails(@RequestBody Reward reward, HttpServletRequest request){
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		try {
			String response = appraisalDetailsService.saveProjectRewardDetails(reward);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}
	@PreAuthorize("auth.allow('GET_ALL_MONTHLY_SALARY_DETAILS')")
	@GetMapping("/getAllMonthlySalaryDetails")
	public ResponseEntity<?> getAllSalaryDetails() {
		LOGGER.info("PayrollService: SalaryDetailsController:Getting all Monthly Salary Details Info level log msg");
		List<MonthSalaryDTO> monthSalaryResponse =monthlySalaryService.getAllMonthlySalaryDetails(); 
		if(!monthSalaryResponse.isEmpty()) {
			return new ResponseEntity<>(monthSalaryResponse, HttpStatus.OK);
		}
		 return new ResponseEntity<>("No salary details found for", HttpStatus.NOT_FOUND);
	}
}






