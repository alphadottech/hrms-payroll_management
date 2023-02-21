package com.alphadot.payroll.controller;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.alphadot.payroll.model.LeaveModel;
import com.alphadot.payroll.model.LeaveRequestModel;
import com.alphadot.payroll.model.OnLeaveRequestSaveEvent;
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
	

	@GetMapping("/getAllEmpLeaves")
    public ResponseEntity<List<LeaveModel>> getAllLeaves() throws ParseException {
		
		return new ResponseEntity<>(leaveService.getAllEmpLeave(), HttpStatus.OK);
    }
	

	@PostMapping("/saveLeave")
	public ResponseEntity<String> saveLeaveBalance(@RequestBody LeaveModel leaveModel) {
		
		return new ResponseEntity<>(leaveService.saveLeave(leaveModel), HttpStatus.OK);
	}
	
	@GetMapping("/getById/{empId}")
	public ResponseEntity<String> getEmpLeaves(@PathVariable("empId") int empId) {
		return new ResponseEntity<>(leaveService.getLeaveById(empId), HttpStatus.OK);
	}
	
	@PutMapping("/updateLeave/{empId}/{leaveCount}")
	public ResponseEntity<String> updateEmpLeaves(@PathVariable("empId") int empId,@PathVariable("leaveCount") int leaveCount) {
		return new ResponseEntity<>(leaveService.updateEmpLeaves(empId,leaveCount), HttpStatus.OK);
	}
	
	@PostMapping("/leaveRequest")
	public ResponseEntity<String> saveLeaveRequest(@RequestBody LeaveRequestModel lr) {
		return new ResponseEntity<>(leaveRequestService.saveLeaveRequest(lr), HttpStatus.OK);
		}
	
	@GetMapping("/getLeaveDetails")
	public ResponseEntity<List<LeaveRequestModel>> getLeaveDetails() {
		return new ResponseEntity<>(leaveRequestService.getLeaveDetails(), HttpStatus.OK);
	}
	
	@GetMapping("getAllLeaveByEmpId/{empid}")
	public ResponseEntity<List<LeaveRequestModel>> getLeaveRequestDetailsByEmpId(@PathVariable("empid") int empid){
		return new ResponseEntity<>(leaveRequestService.getLeaveRequestDetailsByEmpId(empid),HttpStatus.OK);
	}
	
	@GetMapping("/leave/Accepted/{empid}/{leaveId}")
	public ResponseEntity<String> AcceptLeaveRequest(@PathVariable("empid") Integer empid, @PathVariable("leaveId") Integer leaveId) {
		return new ResponseEntity<>(leaveRequestService.AcceptLeaveRequest(empid, leaveId), HttpStatus.OK);		
	}
	
	@GetMapping("/leave/Rejected/{empid}/{leaveId}")
	public ResponseEntity<String> RejectLeaveRequest(@PathVariable("empid") Integer empid, @PathVariable("leaveId") Integer leaveId) {
		return new ResponseEntity<>(leaveRequestService.RejectLeaveRequest(empid, leaveId), HttpStatus.OK);		
	}
	
	
}
