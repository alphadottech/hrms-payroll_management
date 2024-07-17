package com.adt.payroll.controller;

import java.io.IOException;
import java.text.ParseException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.adt.payroll.dto.SalaryDTO;
import com.adt.payroll.dto.SalaryDetailsDTO;
import com.adt.payroll.dto.ViewPaySlipDto;
import com.adt.payroll.model.MonthlySalaryDetails;
import com.adt.payroll.model.PaySlip;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.service.PayRollService;
import com.itextpdf.text.DocumentException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class PayRollController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PayRollService payRollService;

	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@PreAuthorize(" @auth.allow('CREATE_PAYSLIP',T(java.util.Map).of('currentUser', #empId))")
	@GetMapping("/slip")
	public ResponseEntity<PaySlip> payrollCreate(@RequestParam("empId") int empId, @RequestParam("month") String month,
			@RequestParam("year") String year, HttpServletRequest request)
			throws ParseException, IOException, InvalidFormatException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return ResponseEntity.ok(payRollService.createPaySlip(empId, month, year));
	}

	@PreAuthorize("@auth.allow('GENERATE_PAYSLIP_FOR_ALL_EMPLOYEE')")
	@PostMapping("/genPayAll")
	public ResponseEntity<Object> generatePaySlip(@RequestParam("file") MultipartFile file,
			@RequestParam(name = "email", required = false) String email, HttpServletRequest request)
			throws IOException, ParseException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return new ResponseEntity<>(payRollService.generatePaySlip(file, email), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('VIEW_PAYSLIP')")
	@GetMapping("/viewPay")
	public ResponseEntity<Object> viewPay(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("empId") int empId, @RequestParam("month") String month, @RequestParam("year") String year)
			throws ParseException, IOException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		try {
			ViewPaySlipDto viewPaySlipDto = payRollService.viewPay(empId, month, year);
			return new ResponseEntity<>(viewPaySlipDto, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Error occurred: ", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

//	@PreAuthorize("@auth.allow('VIEW_PAYSLIP')")
//	@PostMapping("/viewPay")
//	public ResponseEntity<Object> viewPay(HttpServletRequest request, HttpServletResponse response,
//			@RequestBody SalaryModel salaryModel, @RequestParam("month") String month,
//			@RequestParam("year") String year) throws ParseException, IOException {
//		LOGGER.info("API Call From IP: " + request.getRemoteHost());
//		response.setContentType("application/pdf");
//		byte[] payPdf = payRollService.viewPay(salaryModel, month, year);
//		String base64String = Base64.getEncoder().encodeToString(payPdf);
//		return new ResponseEntity<>(base64String, HttpStatus.OK);
//	}

	@PreAuthorize("@auth.allow('UPDATE_NET_AMOUNT_IN_EXCEL')")
	@PostMapping("/calculateNetAmtPayable")
	public ResponseEntity<String> updateNetAmountInExcel(@RequestParam("file") MultipartFile file,
			HttpServletRequest request) throws IOException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return ResponseEntity.ok(payRollService.updateNetAmountInExcel(file));
	}

	@PreAuthorize("@auth.allow('GENERATE_PAYSLIP_FOR_ALL_EMPLOYEE_FROM_DB')")
	@GetMapping("/generatePaySlipForAll")
	public ResponseEntity<Object> generatePaySlipForAllEmployees(HttpServletRequest request,
			@RequestParam("emailInput") String emailInput) throws IOException, ParseException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return new ResponseEntity<>(payRollService.generatePaySlipForAllEmployees(emailInput), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_EMPLOYEE_PAYROLL_SALARY_DETAILS_BY_EMP_ID')")
	@GetMapping("/getEmployeePayrollSalaryDetailsByEmpId/{empId}")
	public ResponseEntity<SalaryDetailsDTO> getEmployeePayrollSalaryDetailsByEmpId(@PathVariable("empId") Integer empId,
			HttpServletRequest request) {
		LOGGER.info(
				"Payroll-service: PayRollController: getEmployeePayrollDetailsById info level log message AND API Call From IP: "
						+ request.getRemoteHost());
		return ResponseEntity.ok(payRollService.getEmployeePayrollSalaryDetailsByEmpId(empId));
	}
	
	@PreAuthorize("@auth.allow('VERIFY_VALUES')")
	@PostMapping("/validateData/{empId}")
	public ResponseEntity<Object> validatedAmount(@RequestBody SalaryDTO dto) throws IOException, ParseException {

		LOGGER.info("PayrollService: SalaryDetailsController:Getting all Monthly Salary Details Info level log msg");
		ResponseEntity<Object> monthSalaryResponse =payRollService.validateAmount(dto.getEmpId(), dto);
		 return monthSalaryResponse;
	}
	
	@PreAuthorize("@auth.allow('REGENERATE_PAYSLIP')")
	@PostMapping("/regeneratePayslip/{empId}")
	public ResponseEntity<String> regenerateEmployeePayslip(@RequestBody MonthlySalaryDetails dto) throws IOException, DocumentException {

		LOGGER.info("PayrollService: SalaryDetailsController:Getting all Monthly Salary Details Info level log msg");
		String monthSalaryResponse =payRollService.regenerateEmployeePayslip(dto.getEmpId(), dto);
		 return new ResponseEntity<>(monthSalaryResponse, HttpStatus.OK);
	}
}
