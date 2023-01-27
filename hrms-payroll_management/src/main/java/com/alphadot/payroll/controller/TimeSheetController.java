package com.alphadot.payroll.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;


import com.alphadot.payroll.service.TimeSheetService;

@RestController
public class TimeSheetController {
	
	@Autowired
	private TimeSheetService timeSheetService;
	

	
	   @PostMapping("/checkIn/{id}")
	    public ResponseEntity<String> saveCheckIn(@PathVariable int id) throws ParseException {

	        return ResponseEntity.ok(timeSheetService.updateCheckIn(id));
	    }

	   
	   @PutMapping("/checkOut/{id}")
		public ResponseEntity<String> saveCheckOut(@PathVariable int id) throws ParseException {
			
			return new ResponseEntity<>(timeSheetService.updateCheckOut(id), HttpStatus.OK);
		}
	   
	   @GetMapping("/checkStatus/{empId}")
		public ResponseEntity<Boolean> checkStatus(@PathVariable int empId) {
			return new ResponseEntity<>(timeSheetService.saveStatus(empId), HttpStatus.OK);
		}
}
