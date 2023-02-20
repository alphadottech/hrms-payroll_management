package com.alphadot.payroll.controller;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.msg.ResponseModel;
import com.alphadot.payroll.service.TimeSheetService;

@RestController
@RequestMapping("/timeSheet")
public class TimeSheetController {
	
	private static final Logger log=LogManager.getLogger(TimeSheetController.class);
	@Autowired
	private TimeSheetService timeSheetService;
	
	   @PostMapping("/checkIn/{id}")
	    public ResponseEntity<ResponseModel> saveCheckIn(@PathVariable int id) throws ParseException {
          log.info("TimeSheetController: inside checkIn method");
	     
          return ResponseEntity.ok(timeSheetService.updateCheckIn(id));
	    }
	   
	   
	   
	   @PutMapping("/checkOut/{id}")
		public ResponseEntity<ResponseModel> saveCheckOut(@PathVariable int id) throws ParseException {
	          log.info("TimeSheetController: inside checkOut method");
		
	          return new ResponseEntity<>(timeSheetService.updateCheckOut(id), HttpStatus.OK);
		}
	   
	   
	   
	   @PostMapping("/checkStatus/{empId}")
		public ResponseEntity<ResponseModel> checkStatus(@PathVariable int empId) {
	          log.info("TimeSheetController: inside checkStatus method");
	
	          return new ResponseEntity<>(timeSheetService.checkStatus(empId), HttpStatus.OK);
		}
	   
	   
	   @GetMapping("/priorTimeAdjustment/{empId}")
		public ResponseEntity<ResponseModel> priorTimeAdjustment(@PathVariable int empId) {
	          log.info("TimeSheetController: inside priorTimeAdjustment method");
		
	          return new ResponseEntity<>(timeSheetService.checkPriorStatus(empId), HttpStatus.OK);
		}
	   
	   
	   
	   
	   @GetMapping("/empAttendence")
		public ResponseEntity<List<TimeSheetModel>> empAttendence(
				@RequestParam("empId") int empId ,
				@RequestParam("fromDate")@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate, 
				@RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate)
	   {
	          log.info("TimeSheetController: inside empAttendenceData method");
	          return new ResponseEntity<>(timeSheetService.empAttendence(empId,fromDate,toDate), HttpStatus.OK);
		}
	   
	   @GetMapping("/allEmpAttendence")
		public ResponseEntity<List<TimeSheetModel>> allEmpAttendence(
				@RequestParam("fromDate")@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate, 
				@RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate)
	    {
	          log.info("TimeSheetController: inside priorTimeAdjustment method");
		
	          return new ResponseEntity<>(timeSheetService.allEmpAttendence(fromDate,toDate), HttpStatus.OK);
		}
}
