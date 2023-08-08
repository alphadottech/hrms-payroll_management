package com.adt.payroll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.repository.LeaveRepository;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {
	
	@Mock
	private LeaveRepository leaveRepo;
	
	private LeaveService leaveService;
	
	private LeaveModel getLeaveModel() {
		LeaveModel leaveModel = new LeaveModel();
		leaveModel.setEmpId(70);
		leaveModel.setName("Adam Zampa");
		leaveModel.setLeaveBalance(10);
		return leaveModel;
	}
	
	@BeforeEach
	void setup() {
		this.leaveService = new LeaveServiceImpl(leaveRepo);
	}
	
	@Test
	@DisplayName("JUnit test for getAllEmpLeave() method")
	void verify_getAllEmpLeave() {
		
		List<LeaveModel> list = Collections.singletonList(getLeaveModel());
		
		when(leaveRepo.findAll()).thenReturn(list);
		assertEquals(list, leaveService.getAllEmpLeave());
		
	}
	
	@Test
	@DisplayName("JUnit test for getLeaveById() method")
	void verify_getLeaveById() {
		this.leaveService.getLeaveById(1);
		verify(leaveRepo).findByEmpId(1);
	}

}
