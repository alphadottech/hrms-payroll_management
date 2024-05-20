package com.adt.payroll.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.adt.payroll.dto.CheckStatusDTO;
import com.adt.payroll.dto.EmployeeExpenseDTO;
import com.adt.payroll.dto.TimesheetDTO;
import com.adt.payroll.event.OnEmployeeExpenseAcceptOrRejectEvent;
import com.adt.payroll.event.OnEmployeeExpenseDetailsSavedEvent;
import com.adt.payroll.event.OnPriorTimeAcceptOrRejectEvent;
import com.adt.payroll.event.OnPriorTimeDetailsSavedEvent;
import com.adt.payroll.exception.PriorTimeAdjustmentException;
import com.adt.payroll.model.EmployeeExpense;
import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.payload.ApiResponse;
import com.adt.payroll.model.payload.PriorTimeManagementRequest;
import com.adt.payroll.msg.ResponseModel;
import com.adt.payroll.repository.PriorTimeRepository;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.service.TimeSheetService;
import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/timeSheet")
public class TimeSheetController {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TimeSheetService timeSheetService;

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    TimeSheetRepo timeSheetRepo;

    @Autowired
    PriorTimeRepository priorTimeRepository;
    
    @Value("${-Dmy.port}")
	private String serverPort;

	@Value("${-Dmy.property}")
	private String ipaddress;
	
	@Value("${-UI.scheme}")
	private String scheme;

	@Value("${-UI.context}")
	private String context;

    @PreAuthorize("@auth.allow('CHECK_IN',T(java.util.Map).of('currentUser', #empId))")
    @PostMapping("/checkIn/{empId}")
    public ResponseEntity<String> saveCheckIn(@RequestParam("Latitude") double latitude, @RequestParam("Longitude") double longitude, @PathVariable int empId, HttpServletRequest request)
            throws ParseException {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return ResponseEntity.ok(timeSheetService.updateCheckIn(empId, latitude, longitude));
    }

    @PreAuthorize("@auth.allow('CHECK_OUT',T(java.util.Map).of('currentUser', #empId))")
    @PutMapping("/checkOut/{empId}")
    public ResponseEntity<String> saveCheckOut(@RequestParam("Latitude") double latitude, @RequestParam("Longitude") double longitude, @PathVariable int empId, HttpServletRequest request)
            throws ParseException {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.updateCheckOut(empId, latitude, longitude), HttpStatus.OK);
    }

    @PreAuthorize("@auth.allow('CHECK_STATUS_FROM_TIMESHEET',T(java.util.Map).of('currentUser', #empId))")
    @PostMapping("/checkStatus/{empId}")
    public ResponseEntity<CheckStatusDTO> checkStatus(@PathVariable int empId, HttpServletRequest request) {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.checkStatus(empId), HttpStatus.OK);
    }

    @PreAuthorize("@auth.allow('CHECK_PRIORTIME_STATUS',T(java.util.Map).of('currentUser', #empId))")
    @GetMapping("/priorTimeAdjustment/{empId}")
    public ResponseEntity<List<ResponseModel> > priorTimeAdjustment(@PathVariable int empId, HttpServletRequest request) {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.checkPriorStatus(empId), HttpStatus.OK);
    }

@PreAuthorize("@auth.allow('GET_ATTENDANCE_BY_EMPLOYEE_ID',T(java.util.Map).of('currentUser', #empId))")
@GetMapping("/empAttendence")
public ResponseEntity<List<TimesheetDTO>> empAttendence(@RequestParam("empId") int empId,
                                                        @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
                                                        @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
                                                        HttpServletRequest request) {
    LOGGER.info("API Call From IP: " + request.getRemoteHost());

    return new ResponseEntity<>(timeSheetService.empAttendence(empId, fromDate, toDate), HttpStatus.OK);
}


    @PreAuthorize("@auth.allow('GET_ALL_EMPLOYEE_ATTENDANCE')")
    @GetMapping("/allEmpAttendence")
    public ResponseEntity<List<TimeSheetModel>> allEmpAttendence(
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            HttpServletRequest request) {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.allEmpAttendence(fromDate, toDate), HttpStatus.OK);
    }

    @PreAuthorize("@auth.allow('PRIORTIME_ADJUSTMENT_REQUEST')")
    @PostMapping("/updatePriorTime")
    public ResponseEntity<ApiResponse> updatePriorTimeByDate(@RequestParam("Latitude") double latitude, @RequestParam("Longitude") double longitude,@RequestBody PriorTimeManagementRequest priorTimeManagementRequest,
                                                             HttpServletRequest request) throws ParseException {

        LOGGER.info("API Call From IP: " + request.getRemoteHost());
         
        return ((Optional<Priortime>) timeSheetService.savePriorTime(priorTimeManagementRequest,latitude,longitude)).map(priorTimeuser -> {
            int priortimeId = priorTimeuser.getPriortimeId();
            UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.newInstance()
					.scheme(scheme)
					.host(ipaddress)
					.port(serverPort)
					.path(context+"/payroll/timeSheet/updatePriorTime/Accepted/" + priortimeId);
            UriComponentsBuilder urlBuilder2 = ServletUriComponentsBuilder.newInstance()
					.scheme(scheme)
					.host(ipaddress)
					.port(serverPort)
					.path(context+"/payroll/timeSheet/updatePriorTime/Rejected/" + priortimeId);
            OnPriorTimeDetailsSavedEvent onPriorTimeDetailsSavedEvent = new OnPriorTimeDetailsSavedEvent(priorTimeuser,
                    urlBuilder1, urlBuilder2);
            applicationEventPublisher.publishEvent(onPriorTimeDetailsSavedEvent);

            return ResponseEntity.ok(new ApiResponse(true, "Mail sent successfully."));
      
        }).orElseThrow(() -> new PriorTimeAdjustmentException(priorTimeManagementRequest.getEmail(),
                "Missing user details in database"));
    }

 
    @GetMapping("/updatePriorTime/Accepted/{priortimeId}")
    public ResponseEntity<ApiResponse> updatePriorTimeAccepted(@PathVariable(name = "priortimeId") int priortimeId,
                                                               HttpServletRequest request) throws ParseException {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
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
    public ResponseEntity<ApiResponse> updatePriorTimeRejected(@PathVariable(name = "priortimeId") int priortimeId,
                                                               HttpServletRequest request) {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        Optional<Priortime> priortime = priorTimeRepository.findById(priortimeId);
        priortime.get().setStatus("Rejected");
        priorTimeRepository.save(priortime.get());
        OnPriorTimeAcceptOrRejectEvent onPriortimeApprovalEvent = new OnPriorTimeAcceptOrRejectEvent(priortime,
                "PriorTimesheet Entry", "Rejected");

        applicationEventPublisher.publishEvent(onPriortimeApprovalEvent);
        return ResponseEntity.ok(new ApiResponse(true, "Details for PriorTime Timesheet entry updated as Rejected"));

    }

    @PreAuthorize("@auth.allow('PAUSE_TIME',T(java.util.Map).of('currentUser', #empId))")
    @PutMapping("/pause/{empId}")
    public ResponseEntity<String> pauseWorkingTime(@PathVariable int empId, HttpServletRequest request) {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.pauseWorkingTime(empId), HttpStatus.OK);
    }

    @PreAuthorize("@auth.allow('RESUME_TIME',T(java.util.Map).of('currentUser', #empId))")
    @PatchMapping("/resume/{empId}")
    public ResponseEntity<String> resumeWorkingTime(@PathVariable int empId, HttpServletRequest request)
            throws ParseException {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return new ResponseEntity<>(timeSheetService.resumeWorkingTime(empId), HttpStatus.OK);
    }

    @PreAuthorize("@auth.allow('EMPLOYEE_EXPENSE_SAVE_REQUEST')")
    @PostMapping("/employeeExpense/{empId}")
    public ResponseEntity employeeExpense(@PathVariable int empId, @RequestParam("expense") String expense, @RequestParam List<MultipartFile> invoice, HttpServletRequest request)
            throws ParseException, IOException {

        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        Gson gson = new Gson();
        EmployeeExpense employeeExpenseRequest = gson.fromJson(expense, EmployeeExpense.class);
        EmployeeExpenseDTO employeeExpenseDTO = timeSheetService.employeeExpense(empId, invoice, employeeExpenseRequest);

        UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/timeSheet/employeeExpenseApprove/" + employeeExpenseDTO.getExpenseId());
        UriComponentsBuilder urlBuilder2 = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/timeSheet/employeeExpenseReject/" + employeeExpenseDTO.getExpenseId());
        OnEmployeeExpenseDetailsSavedEvent onEmployeeExpenseDetailsSavedEvent = new OnEmployeeExpenseDetailsSavedEvent(employeeExpenseDTO,
                urlBuilder1, urlBuilder2);
        applicationEventPublisher.publishEvent(onEmployeeExpenseDetailsSavedEvent);
        return ResponseEntity.ok(new ApiResponse(true, "Expense Submitted Successfully."));
    }

    @PreAuthorize("@auth.allow('APPROV_EEMPLOYEE_EXPENSE')")
    @PutMapping("/employeeExpenseApprove/{expenseId}")
    public ResponseEntity<String> ApproveEmployeeExpense(@PathVariable int expenseId, HttpServletRequest request) {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return ResponseEntity.ok(timeSheetService.approveEmployeeExpenseById(expenseId));

    }

    @PutMapping("/employeeExpenseReject/{expenseId}")
    public ResponseEntity<String> RejectEmployeeExpense(@PathVariable int expenseId, HttpServletRequest request) {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return ResponseEntity.ok(timeSheetService.rejectEmployeeExpenseById(expenseId));

    }

    @PreAuthorize("@auth.allow('UPDATE_EMPLOYEE_EXPENSE')")
    @PutMapping("/employeeExpense")
    public ResponseEntity<ApiResponse> EmployeeExpense(@RequestBody EmployeeExpenseDTO employeeExpenseDTO, HttpServletRequest request)
            throws ParseException, IOException {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        EmployeeExpenseDTO employeeExpenseDTO1 = timeSheetService.acceptedEmployeeExpense(employeeExpenseDTO.getExpenseId(), employeeExpenseDTO);
        OnEmployeeExpenseAcceptOrRejectEvent onEmployeeExpenseAcceptOrRejectEvent = new OnEmployeeExpenseAcceptOrRejectEvent(employeeExpenseDTO,
                "Expenses Entry", "Approved");
        applicationEventPublisher.publishEvent(onEmployeeExpenseAcceptOrRejectEvent);
        return ResponseEntity.ok(new ApiResponse(true, "Details for Employee Expense updated successfully"));
    }

    @GetMapping("/getAllExpense")
    public ResponseEntity<List<EmployeeExpense>> getAllExpenseDetail(HttpServletRequest request) {
        LOGGER.info("API Call From IP: " + request.getRemoteHost());
        return ResponseEntity.ok(timeSheetService.getAllExpenseDetail());

    }

    @GetMapping("/exporttoexcel")
    public ResponseEntity<Resource> allEmpAttendenceExportExcel() throws IOException {
        String filename="alphadot_exceldata.xlsx";

        ByteArrayInputStream newdata = timeSheetService.getExcelData();

        InputStreamResource file=new InputStreamResource(newdata);
        ResponseEntity<Resource> body  =ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+filename).contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
        return body;
    }



}
