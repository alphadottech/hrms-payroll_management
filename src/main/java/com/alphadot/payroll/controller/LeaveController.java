package com.alphadot.payroll.controller;

import java.text.ParseException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alphadot.payroll.model.LeaveModel;
import com.alphadot.payroll.model.LeaveRequestModel;
import com.alphadot.payroll.repository.LeaveRequestRepo;
import com.alphadot.payroll.service.LeaveRequestService;
import com.alphadot.payroll.service.LeaveService;

@RestController
@RequestMapping("/leave")
public class LeaveController {

	@Autowired
	public LeaveService leaveService;
	
	@Autowired
	private LeaveRequestService leaveRequestService;
	
	@Autowired
	ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	LeaveRequestRepo leaveRequestRepo;
	
	private static final Logger log=LogManager.getLogger(LeaveController.class);

	
	@GetMapping("/getAllEmpLeaves")
    public ResponseEntity<List<LeaveModel>> getAllLeaves() throws ParseException {
	log.info("Payroll service: leave:  getAllLeaves Info level log msg");	
		return new ResponseEntity<>(leaveService.getAllEmpLeave(), HttpStatus.OK);
    }
	

	
	@GetMapping("/getById/{empId}")
	public ResponseEntity<LeaveModel> getEmpLeaves(@PathVariable("empId") int empId) {
		log.info("Payroll service: leave:  getEmpLeaves Info level log msg");
		return new ResponseEntity<>(leaveService.getLeaveById(empId), HttpStatus.OK);
	}

	
	@PostMapping("/leaveRequest")
	public ResponseEntity<String> saveLeaveRequest(@RequestBody LeaveRequestModel lr) {
		log.info("Payroll service: leave:  saveLeaveRequest Info level log msg");
		return new ResponseEntity<>(leaveRequestService.saveLeaveRequest(lr), HttpStatus.OK);
		}
	
	@GetMapping("/getLeaveDetails")
	public ResponseEntity<List<LeaveRequestModel>> getLeaveDetails() {
		log.info("Payroll service: leave:  getLeaveDetails Info level log msg");
		return new ResponseEntity<>(leaveRequestService.getLeaveDetails(), HttpStatus.OK);
	}
	
	@GetMapping("getAllLeaveByEmpId/{empid}")
	public ResponseEntity<List<LeaveRequestModel>> getLeaveRequestDetailsByEmpId(@PathVariable("empid") int empid){
		log.info("Payroll service: leave:  getLeaveRequestDetailsByEmpId Info level log msg");
		return new ResponseEntity<>(leaveRequestService.getLeaveRequestDetailsByEmpId(empid),HttpStatus.OK);
	}
	
	@GetMapping("/leave/Accepted/{empid}/{leaveId}")
	public ResponseEntity<String> AcceptLeaveRequest(@PathVariable("empid") Integer empid, @PathVariable("leaveId") Integer leaveId) {
		log.info("Payroll service: leave:  AcceptLeaveRequest Info level log msg");
		return new ResponseEntity<>(leaveRequestService.AcceptLeaveRequest(empid, leaveId), HttpStatus.OK);		
	}
	
	@GetMapping("/leave/Rejected/{empid}/{leaveId}")
	public ResponseEntity<String> RejectLeaveRequest(@PathVariable("empid") Integer empid, @PathVariable("leaveId") Integer leaveId) {
		log.info("Payroll service: leave:  RejectLeaveRequest Info level log msg");
		return new ResponseEntity<>(leaveRequestService.RejectLeaveRequest(empid, leaveId), HttpStatus.OK);		
	}
	
	
}
