package com.adt.payroll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.repository.LeaveRepository;
import com.adt.payroll.repository.LeaveRequestRepo;
import com.adt.payroll.repository.UserRepo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LeaveRequestServiceTest {

	@Mock
	private LeaveRequestRepo leaveRequestRepo;
	
	@Mock
	private LeaveRepository leaveRepo;
	
	private LeaveRequestService leaveReqService;
	
	@InjectMocks
	private LeaveRequestServiceImpl leaveReqServiceImpl;
	
	@Mock
	private UserRepo userRepo; 
	
	private LeaveRequestModel getLeaveReqModel() {
		LeaveRequestModel requestModel = new LeaveRequestModel();
		
		requestModel.setEmpid(118);
		requestModel.setLeaveBalance(10);
		requestModel.setLeaveid(121);
		requestModel.setStatus("Pending");
		requestModel.setLeaveType("");
		requestModel.setLeaveReason("Fever");
		requestModel.setName("Anish");
		requestModel.setLeavedate(List.of("2023/07/10", "2023/07/11"));
		
		return requestModel;
	}
	
	private LeaveModel getLeaveModel() {
		LeaveModel leaveModel = new LeaveModel();
		leaveModel.setEmpId(70);
		leaveModel.setName("Adam Zampa");
		leaveModel.setLeaveBalance(10);
		return leaveModel;
	}
	
	@BeforeEach
	void setup() {
		this.leaveReqService = new LeaveRequestServiceImpl(leaveRequestRepo);
	}
	
	@Test
	@DisplayName("JUnit test for getLeaveDetails() method")
	void verify_getLeaveDetails() {
		this.leaveReqService.getLeaveDetails();
		verify(leaveRequestRepo).findAll();
	}
	
	@Test
	@DisplayName("JUnit test for getLeaveRequestDetailsByEmpId() method")
	void verify_getLeaveRequestDetailsByEmpId() {
		this.leaveReqService.getLeaveRequestDetailsByEmpId(0,10,1);
		verify(leaveRequestRepo).findByempid(1);
	}
	
}
