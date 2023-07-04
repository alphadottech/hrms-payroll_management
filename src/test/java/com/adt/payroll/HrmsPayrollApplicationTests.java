package com.adt.payroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.repository.LeaveRepository;
import com.adt.payroll.service.LeaveService;

@SpringBootTest
class HrmsPayrollApplicationTests {

	@Test
	void contextLoads() {
	}


	@Autowired
	private LeaveService leveService;

	@MockBean
	private LeaveRepository leaveRepo;

	public void getAllEmpLeave() {
//		when(leaveRepo.findAll()).thenReturn(Stream.of(new LeaveModel(1,1,1)).collect(Collectors.toList()));
		assertEquals(1, leveService.getAllEmpLeave().size());
	}

}
