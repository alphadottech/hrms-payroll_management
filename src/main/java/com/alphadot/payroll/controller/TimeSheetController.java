package com.alphadot.payroll.controller;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.alphadot.payroll.event.OnPriorTimeDetailsSavedEvent;
import com.alphadot.payroll.exception.PriorTimeAdjustmentException;
import com.alphadot.payroll.model.Priortime;
import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.model.payload.ApiResponse;
import com.alphadot.payroll.model.payload.PriorTimeManagementRequest;
import com.alphadot.payroll.msg.ResponseModel;
import com.alphadot.payroll.repository.PriorTimeRepository;
import com.alphadot.payroll.repository.TimeSheetRepo;
import com.alphadot.payroll.service.TimeSheetService;

@RestController
@RequestMapping("/timeSheet")
public class TimeSheetController {
	
	private static final Logger log=LogManager.getLogger(TimeSheetController.class);
	@Autowired
	private TimeSheetService timeSheetService;

	@Autowired
	ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	TimeSheetRepo timeSheetRepo;

	@Autowired
	PriorTimeRepository priorTimeRepository;
	
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
	   
	   @PostMapping("/updatePriorTime")
		public ResponseEntity updatePriorTimeByDate(@RequestBody PriorTimeManagementRequest priorTimeManagementRequest)
				throws ParseException {

			return ((Optional<Priortime>) timeSheetService.savePriorTime(priorTimeManagementRequest)).map(priorTimeuser -> {
				int priortimeId=priorTimeuser.getPriortimeId();
	            UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/timeSheet/updatePriorTime/Accepted/"+priortimeId);
				UriComponentsBuilder urlBuilder2 = ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/timeSheet/updatePriorTime/Rejected/"+priortimeId);
				OnPriorTimeDetailsSavedEvent onPriorTimeDetailsSavedEvent = new OnPriorTimeDetailsSavedEvent(
						priorTimeuser, urlBuilder1, urlBuilder2);
				applicationEventPublisher.publishEvent(onPriorTimeDetailsSavedEvent);
				return ResponseEntity.ok(new ApiResponse(true, "Mail sent successfully."));
			}).orElseThrow(
					() -> new PriorTimeAdjustmentException(priorTimeManagementRequest.getEmail(), "Missing user details in database"));
		}


		@GetMapping("/updatePriorTime/Accepted/{priortimeId}") 
		public String updatePriorTimeAccepted(@PathVariable(name = "priortimeId") int priortimeId ) throws ParseException {
			Optional<Priortime> priortime = priorTimeRepository.findById(priortimeId);
	        timeSheetService.saveConfirmedDetails(priortime);
	        priortime.get().setStatus("Accepted");
	        priorTimeRepository.save(priortime.get());
		
			return "Accepted";
		}

		@GetMapping("/updatePriorTime/Rejected/{priortimeId}")
		public String updatePriorTimeRejected(@PathVariable(name = "priortimeId") int priortimeId) {
	        Optional<Priortime> priortime = priorTimeRepository.findById(priortimeId);
		    priortime.get().setStatus("Rejected");
			priorTimeRepository.save(priortime.get());

	        return "Rejected";
		}
}
