
package com.adt.payroll.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.repository.LeaveRequestRepo;
import com.adt.payroll.service.LeaveRequestService;
import com.adt.payroll.service.LeaveService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/leave")
public class LeaveController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public LeaveService leaveService;

	@Autowired
	private LeaveRequestService leaveRequestService;

	@Autowired
	ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	LeaveRequestRepo leaveRequestRepo;

	@PreAuthorize("@auth.allow('GET_ALL_EMPLOYEE_LEAVE_BALANCE')")
	@GetMapping("/getAllEmpLeaves")
	public ResponseEntity<List<LeaveModel>> getAllLeaves() throws ParseException {
		LOGGER.info("Payroll service: leave:  getAllLeaves Info level log msg");
		return new ResponseEntity<>(leaveService.getAllEmpLeave(), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_EMPLOYEE_LEAVES_BALANCE_BY_ID',T(java.util.Map).of('currentUser', #empId))")
	@GetMapping("/getById/{empId}")
	public ResponseEntity<LeaveModel> getEmpLeaves(@PathVariable("empId") int empId) {
		LOGGER.info("Payroll service: leave:  getEmpLeaves Info level log msg");
		return new ResponseEntity<>(leaveService.getLeaveById(empId), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('SAVE_LEAVE_REQUEST')")
	@PostMapping("/leaveRequest")
	public ResponseEntity<String> saveLeaveRequest(@RequestBody LeaveRequestModel lr) {
		LOGGER.info("Payroll service: leave:  saveLeaveRequest Info level log msg");
		return new ResponseEntity<>(leaveRequestService.saveLeaveRequest(lr), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_LEAVE_DETAILS')")
	@GetMapping("/getLeaveDetails")
	public ResponseEntity<List<LeaveRequestModel>> getLeaveDetails() {
		LOGGER.info("Payroll service: leave:  getLeaveDetails Info level log msg");
		return new ResponseEntity<>(leaveRequestService.getLeaveDetails(), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_LEAVE_REQUEST_DETAILS_BY_EMPLOYEE_ID',T(java.util.Map).of('currentUser', #empId))")
	@GetMapping("getAllLeaveByEmpId/{empId}")
	public ResponseEntity<List<LeaveRequestModel>> getLeaveRequestDetailsByEmpId(@PathVariable("empId") int empId) {
		LOGGER.info("Payroll service: leave:  getLeaveRequestDetailsByEmpId Info level log msg");
		return new ResponseEntity<>(leaveRequestService.getLeaveRequestDetailsByEmpId(empId), HttpStatus.OK);
	}

	
	@GetMapping("/leave/Accepted/{empid}/{leaveId}/{leaveDates}/{leaveType}/{leaveReason}")
	public ResponseEntity<String> AcceptLeaveRequest(@PathVariable("empid") Integer empid,
			@PathVariable("leaveId") Integer leaveId,@PathVariable("leaveDates") Integer leaveDate,@PathVariable("leaveType") String leaveType,@PathVariable("leaveReason") String leaveReason) throws TemplateException, MessagingException, IOException {
		LOGGER.info("Payroll service: leave:  AcceptLeaveRequest Info level log msg");
		return new ResponseEntity<>(leaveRequestService.AcceptLeaveRequest(empid, leaveId,leaveDate,leaveType,leaveReason), HttpStatus.OK);
	}
	
	
	@GetMapping("/leave/Rejected/{empid}/{leaveId}/{leaveType}/{leaveReason}")
	public ResponseEntity<String> RejectLeaveRequest(@PathVariable("empid") Integer empid,
			@PathVariable("leaveId") Integer leaveId,@PathVariable("leaveType") String leaveType,@PathVariable("leaveReason") String leaveReason) throws TemplateException, MessagingException, IOException {
		LOGGER.info("Payroll service: leave:  RejectLeaveRequest Info level log msg");
		return new ResponseEntity<>(leaveRequestService.RejectLeaveRequest(empid, leaveId,leaveType,leaveReason), HttpStatus.OK);
	}
	
	@PreAuthorize("@auth.allow('GET_ALL_EMPLOYEE_LEAVE_DETAILS')")
	@GetMapping("/getAllEmployeeLeaves")
	public ResponseEntity<List<LeaveRequestModel>> getAllEmployeeLeaveDetails() {
		LOGGER.info("Payroll service: leave:  RejectLeaveRequest Info level log msg");
		return new ResponseEntity<>(leaveRequestService.getAllEmployeeLeaveDetails(), HttpStatus.OK);
	}

}
