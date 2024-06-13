
package com.adt.payroll.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.*;

import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.repository.LeaveRequestRepo;
import com.adt.payroll.service.LeaveRequestService;
import com.adt.payroll.service.LeaveService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/leave")
public class LeaveController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public LeaveService leaveService;

	@Autowired
	private LeaveRequestService leaveRequestService;

	@Autowired
	ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	LeaveRequestRepo leaveRequestRepo;
	
	@Autowired
	 private Configuration freemarkerConfig;
	 
	@Value("${app.velocity.templates.location}")
	private String basePackagePath;

	@PreAuthorize("@auth.allow('GET_ALL_EMPLOYEE_LEAVE_BALANCE')")
	@GetMapping("/getAllEmpLeaves")
	public ResponseEntity<List<LeaveModel>> getAllLeaves() throws ParseException {
		LOGGER.info("Payroll service: leave:  getAllLeaves Info level log msg");
		return new ResponseEntity<>(leaveService.getAllEmpLeave(), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_EMPLOYEE_LEAVES_BALANCE_BY_ID',T(java.util.Map).of('currentUser', #empId))")
	@GetMapping("/getById/{empId}")
	public ResponseEntity<LeaveModel> getEmpLeaves(@PathVariable("empId") int empId) {
		LOGGER.info("Payroll service: leave:  getEmpLeaves Info level log msg");
		return new ResponseEntity<>(leaveService.getLeaveById(empId), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('SAVE_LEAVE_REQUEST')")
	@PostMapping("/leaveRequest")
	public ResponseEntity<String> saveLeaveRequest(@RequestBody LeaveRequestModel lr) {
		LOGGER.info("Payroll service: leave:  saveLeaveRequest Info level log msg");
		return new ResponseEntity<>(leaveRequestService.saveLeaveRequest(lr), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_LEAVE_DETAILS')")
	@GetMapping("/getLeaveDetails")
	public ResponseEntity<List<LeaveRequestModel>> getLeaveDetails() {
		LOGGER.info("Payroll service: leave:  getLeaveDetails Info level log msg");
		return new ResponseEntity<>(leaveRequestService.getLeaveDetails(), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_LEAVE_REQUEST_DETAILS_BY_EMPLOYEE_ID',T(java.util.Map).of('currentUser', #empId))")
	@GetMapping("getAllLeaveByEmpId/{empId}")
	public ResponseEntity<List<LeaveRequestModel>> getLeaveRequestDetailsByEmpId(@PathVariable("empId") int empId) {
		LOGGER.info("Payroll service: leave:  getLeaveRequestDetailsByEmpId Info level log msg");
		return new ResponseEntity<>(leaveRequestService.getLeaveRequestDetailsByEmpId(empId), HttpStatus.OK);
	}

	
	@PreAuthorize("@auth.allow('ACCEPT_LEAVE_REQUEST')")
	@GetMapping("/leave/Accepted/{empid}/{leaveId}/{leaveDates}/{leaveType}/{leaveReason}")
	public ResponseEntity<?> AcceptLeaveRequest(@PathVariable("empid") Integer empid,
			@PathVariable("leaveId") Integer leaveId,@PathVariable("leaveDates") Integer leaveDate,@PathVariable("leaveType") String leaveType,@PathVariable("leaveReason") String leaveRecson) throws TemplateException, MessagingException, IOException {
		Optional<LeaveRequestModel> leaveRequest= leaveRequestRepo.findById(leaveId);
		 freemarkerConfig.setClassForTemplateLoading(getClass(), basePackagePath);
		 Template  template = freemarkerConfig.getTemplate("message.ftl");
		 Map<String, Object> model = new HashMap<>();
		 String status="rejected";
		if(leaveRequest.get().getStatus().equalsIgnoreCase("Pending")) {
		    LOGGER.info("Payroll service: leave:  AcceptLeaveRequest Info level log msg");
		    leaveRequestService.AcceptLeaveRequest(empid, leaveId,leaveDate,leaveType,leaveRecson);   
	        model.put("Message", " leave request has been successfully approved.!");
	        model.put("Email", "");
		return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model), HttpStatus.OK);
		    }
		if(leaveRequest.get().getStatus().equalsIgnoreCase("Accepted")) {
			 status="approved";
		}
		 model.put("Message", " leave request has been already "+status+" by");
		 model.put("Email",leaveRequest.get().getUpdatedBy());
      return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model), HttpStatus.OK);
		}
	
	@PreAuthorize("@auth.allow('REJECT_LEAVE_REQUEST')")
	@GetMapping("/leave/Rejected/{empid}/{leaveId}/{leaveType}/{leaveReason}")
	public ResponseEntity<String> RejectLeaveRequest(@PathVariable("empid") Integer empid,
			@PathVariable("leaveId") Integer leaveId,@PathVariable("leaveType") String leaveType,@PathVariable("leaveReason") String leaveRecson) throws TemplateException, MessagingException, IOException {
		Optional<LeaveRequestModel> leaveRequest = leaveRequestRepo.findById(leaveId);
		freemarkerConfig.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = freemarkerConfig.getTemplate("message.ftl");
		Map<String, Object> model = new HashMap<>();
		String status = "rejected";
		if (leaveRequest.get().getStatus().equalsIgnoreCase("Pending")) {
			LOGGER.info("Payroll service: leave:  RejectLeaveRequest Info level log msg");
			leaveRequestService.RejectLeaveRequest(empid, leaveId, leaveType, leaveRecson);
			model.put("Message", " leave request has been successfully rejected!");
			 model.put("Email", "");
			return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model),
					HttpStatus.OK);
		}
		if (leaveRequest.get().getStatus().equalsIgnoreCase("Accepted")) {
			status = "approved";
		}
		model.put("Message", " leave request has been already " + status + " by");
		model.put("Email", leaveRequest.get().getUpdatedBy());
		return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model), HttpStatus.OK);
	}
	
	@PreAuthorize("@auth.allow('GET_ALL_EMPLOYEE_LEAVE_DETAILS')")
	@GetMapping("/getAllEmployeeLeaves")
	public ResponseEntity<List<LeaveRequestModel>> getAllEmployeeLeaveDetails() {
		LOGGER.info("Payroll service: leave:  RejectLeaveRequest Info level log msg");
		return new ResponseEntity<>(leaveRequestService.getAllEmployeeLeaveDetails(), HttpStatus.OK);
	}

}
