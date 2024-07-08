package com.adt.payroll.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.dto.SearchNameDto;
import com.adt.payroll.model.AppraisalDetails;
import com.adt.payroll.model.Reward;
import com.adt.payroll.service.AppraisalDetailsService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class AppraisalRewardDetailsController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private AppraisalDetailsService appraisalDetailsService;
	
	@PreAuthorize("@auth.allow('GET_EMP_NAME_BY_CHAR')")
	@GetMapping("/getEmpNameByCharacter/{empName}")
	public ResponseEntity<List<SearchNameDto>> getEmployeeSalaryById(HttpServletRequest request, @PathVariable String empName) {
		LOGGER.info("API Call From IP: " , request.getRemoteHost(),"PayrollService: AppraisalRewardDetailsController:start");	
		System.out.println(empName);
		return appraisalDetailsService.getEmployeeNameByCharacter(empName);
	}
	
	@PreAuthorize("@auth.allow('SAVE_APPRAISAL_DETAILS')")
	@PostMapping("/addAppraisalDetails")
	public ResponseEntity<String> addAppraisalDetails(@RequestBody AppraisalDetails appraisalDetails) {
		LOGGER.info("PayrollService: SalaryDetailsController:Employee addAppraisalDetails Info level log msg");
		return appraisalDetailsService.addAppraisalDetails(appraisalDetails);
	}

	@PreAuthorize("@auth.allow('GET_ALL_EMPLOYEE_APPRAISAL_DETAILS')")
	@GetMapping("/getAllEmployeesWithLatestAppraisal")
	public ResponseEntity<Page<AppraisalDetailsDTO>> getAllEmployeesWithLatestAppraisal(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		LOGGER.info("PayrollService: SalaryDetailsController:Getting all employees appraisal details Info level log msg");
		return appraisalDetailsService.getEmployeesWithLatestAppraisal(page, size);
	}
	
	@PreAuthorize("@auth.allow('GET_ALL_APPRAISAL_DETAILS_BY_ID')")
	@GetMapping("/getAllAppraisalDetailsbyId/{id}")
	public ResponseEntity<List<AppraisalDetailsDTO>> getAppraisalDetailsById(@PathVariable Integer id) {
		return appraisalDetailsService.getAppraisalDetails(id);
	}

	@PreAuthorize("@auth.allow('GET_REWARD_DETAILS_BY_ID')")
	@GetMapping("/getRewardDetails/{id}")
	public List<AppraisalDetailsDTO> getRewardDetailByEmployeeId(@PathVariable Integer id,  HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
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
}
