package com.alphadot.payroll.controller;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.alphadot.payroll.dto.CheckStatusDTO;
import com.alphadot.payroll.dto.TimesheetDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.alphadot.payroll.event.OnPriorTimeAcceptOrRejectEvent;
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

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/timeSheet")
public class TimeSheetController {

    private static final Logger log = LogManager.getLogger(TimeSheetController.class);
    @Autowired
    private TimeSheetService timeSheetService;

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    TimeSheetRepo timeSheetRepo;

    @Autowired
    PriorTimeRepository priorTimeRepository;

    @PostMapping("/checkIn/{empId}")
    public ResponseEntity<String> saveCheckIn(@PathVariable int empId, HttpServletRequest request) throws ParseException {
        log.info("API Call From IP: " + request.getRemoteHost());
        return ResponseEntity.ok(timeSheetService.updateCheckIn(empId));
    }


    @PutMapping("/checkOut/{empId}")
    public ResponseEntity<String> saveCheckOut(@PathVariable int empId, HttpServletRequest request) throws ParseException {
        log.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.updateCheckOut(empId), HttpStatus.OK);
    }


    @PostMapping("/checkStatus/{empId}")
    public ResponseEntity<CheckStatusDTO> checkStatus(@PathVariable int empId, HttpServletRequest request) {
        log.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.checkStatus(empId), HttpStatus.OK);
    }


    @GetMapping("/priorTimeAdjustment/{empId}")
    public ResponseEntity<ResponseModel> priorTimeAdjustment(@PathVariable int empId, HttpServletRequest request) {
        log.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.checkPriorStatus(empId), HttpStatus.OK);
    }


    @GetMapping("/empAttendence")
    public ResponseEntity<List<TimesheetDTO>> empAttendence(
            @RequestParam("empId") int empId,
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate, HttpServletRequest request) {
        log.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.empAttendence(empId, fromDate, toDate), HttpStatus.OK);
    }

    @GetMapping("/allEmpAttendence")
    public ResponseEntity<List<TimeSheetModel>> allEmpAttendence(
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate, HttpServletRequest request) {
        log.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.allEmpAttendence(fromDate, toDate), HttpStatus.OK);
    }


    @PostMapping("/updatePriorTime")
    public ResponseEntity updatePriorTimeByDate(@RequestBody PriorTimeManagementRequest priorTimeManagementRequest, HttpServletRequest request)
            throws ParseException {
        log.info("API Call From IP: " + request.getRemoteHost());
        return ((Optional<Priortime>) timeSheetService.savePriorTime(priorTimeManagementRequest)).map(priorTimeuser -> {
            int priortimeId = priorTimeuser.getPriortimeId();
            UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/timeSheet/updatePriorTime/Accepted/" + priortimeId);
            UriComponentsBuilder urlBuilder2 = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/timeSheet/updatePriorTime/Rejected/" + priortimeId);
            OnPriorTimeDetailsSavedEvent onPriorTimeDetailsSavedEvent = new OnPriorTimeDetailsSavedEvent(priorTimeuser,
                    urlBuilder1, urlBuilder2);
            applicationEventPublisher.publishEvent(onPriorTimeDetailsSavedEvent);
            return ResponseEntity.ok(new ApiResponse(true, "Mail sent successfully."));
        }).orElseThrow(() -> new PriorTimeAdjustmentException(priorTimeManagementRequest.getEmail(),
                "Missing user details in database"));
    }

    @GetMapping("/updatePriorTime/Accepted/{priortimeId}")
    public ResponseEntity<ApiResponse> updatePriorTimeAccepted(@PathVariable(name = "priortimeId") int priortimeId, HttpServletRequest request)
            throws ParseException {
        log.info("API Call From IP: " + request.getRemoteHost());
        Optional<Priortime> priortime = priorTimeRepository.findById(priortimeId);
        timeSheetService.saveConfirmedDetails(priortime);
        priortime.get().setStatus("Accepted");
        priorTimeRepository.save(priortime.get());
        String email = priortime.get().getEmail();
        OnPriorTimeAcceptOrRejectEvent onPriortimeApprovalEvent = new OnPriorTimeAcceptOrRejectEvent(priortime,
                "PriorTimesheet Entry", "Approved");

        applicationEventPublisher.publishEvent(onPriortimeApprovalEvent);
        return ResponseEntity.ok(new ApiResponse(true, "Details for PriorTime Timesheet entry updated successfully"));

    }

    @GetMapping("/updatePriorTime/Rejected/{priortimeId}")
    public ResponseEntity<ApiResponse> updatePriorTimeRejected(@PathVariable(name = "priortimeId") int priortimeId, HttpServletRequest request) {
        log.info("API Call From IP: " + request.getRemoteHost());
        Optional<Priortime> priortime = priorTimeRepository.findById(priortimeId);
        priortime.get().setStatus("Rejected");
        priorTimeRepository.save(priortime.get());
        OnPriorTimeAcceptOrRejectEvent onPriortimeApprovalEvent = new OnPriorTimeAcceptOrRejectEvent(priortime,
                "PriorTimesheet Entry", "Rejected");

        applicationEventPublisher.publishEvent(onPriortimeApprovalEvent);
        return ResponseEntity.ok(new ApiResponse(true, "Details for PriorTime Timesheet entry updated as Rejected"));

    }

    @PutMapping("/pause/{empId}")
    public ResponseEntity<String> pauseWorkingTime(@PathVariable int empId, HttpServletRequest request) {
        log.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.pauseWorkingTime(empId), HttpStatus.OK);
    }

    @PatchMapping("/resume/{empId}")
    public ResponseEntity<String> resumeWorkingTime(@PathVariable int empId, HttpServletRequest request) throws ParseException {
        log.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.resumeWorkingTime(empId), HttpStatus.OK);
    }
}
