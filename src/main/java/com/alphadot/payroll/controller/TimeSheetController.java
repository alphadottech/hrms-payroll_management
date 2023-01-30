package com.alphadot.payroll.controller;

import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.alphadot.payroll.service.TimeSheetService;

@RestController
@RequestMapping("/timeSheet")
public class TimeSheetController {
	
	private static final Logger log=LogManager.getLogger(TimeSheetController.class);

	@Autowired
	private TimeSheetService timeSheetService;
	
	   @PostMapping("/checkIn/{id}")
	    public ResponseEntity<String> saveCheckIn(@PathVariable int id) throws ParseException {
          log.info("TimeSheetController: inside checkIn method");
	        return ResponseEntity.ok(timeSheetService.updateCheckIn(id));
	    }
	   
	   @PutMapping("/checkOut/{id}")
		public ResponseEntity<String> saveCheckOut(@PathVariable int id) throws ParseException {
	          log.info("TimeSheetController: inside checkOut method");
			return new ResponseEntity<>(timeSheetService.updateCheckOut(id), HttpStatus.OK);
		}
	   
	   @GetMapping("/checkStatus/{empId}")
		public ResponseEntity<Boolean> checkStatus(@PathVariable int empId) {
	          log.info("TimeSheetController: inside checkStatus method");
			return new ResponseEntity<>(timeSheetService.saveStatus(empId), HttpStatus.OK);
		}
}
