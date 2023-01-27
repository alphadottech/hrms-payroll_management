package com.alphadot.payroll.controller;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.alphadot.payroll.model.LeaveModel;
import com.alphadot.payroll.service.LeaveService;

@RestController
@RequestMapping("/leave")
public class LeaveController {

	@Autowired
	public LeaveService leaveService;
	

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
	
}
