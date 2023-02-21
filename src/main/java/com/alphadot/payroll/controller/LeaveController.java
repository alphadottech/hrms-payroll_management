package com.alphadot.payroll.controller;

import java.text.ParseException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	
}
