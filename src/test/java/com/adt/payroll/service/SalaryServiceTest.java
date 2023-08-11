package com.adt.payroll.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.adt.payroll.model.SalaryModel;
import com.adt.payroll.repository.SalaryRepo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SalaryServiceTest {

	@Mock
	private SalaryRepo salaryRepo;
	
	@InjectMocks
	private SalaryServiceImpl salaryService;
	
	//Given
	private SalaryModel givenSalaryModel() {
		SalaryModel salaryModel = SalaryModel.builder().serialNo(6)
                .accountNumber("10103456789")
                .bankName("HDFC")
                .email("rashi.khanna@gmail.com")
                .empId(18)
                .empName("Rashi Khanna")
                .joinDate("02/02/2023")
                .role("Angular Developer")
                .salary(12000).build();
		return salaryModel;
	}
	
	@Test
	@DisplayName("JUnit test for getAllEmpSalary() method")
	void test_getAllEmpSalary() {
		this.salaryService.getAllEmpSalary();
		verify(salaryRepo).findAll();
		assertThat(this.salaryService.getAllEmpSalary().size()).isEqualTo(salaryRepo.findAll().size());
	}
	
	@Test
	@DisplayName("JUnit test for getSalaryById() method")
	void test_getSalaryById() {
		//Given
		SalaryModel salaryModel = givenSalaryModel();
		
		when(salaryRepo.findByEmpId(1)).thenReturn(Optional.of(salaryModel));
		assertEquals(Optional.of(salaryModel), salaryService.getSalaryById(1));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"pinki sharma"})
	@DisplayName("JUnit test for searchByName() method")
	void test_searchByName(String empName) {
		this.salaryService.searchByName(empName);
		verify(salaryRepo).searchByEmpName(empName);
	}
	
	@Test
	@DisplayName("JUnit test for saveSalary() method")
	void test_saveSalary() {

		SalaryModel salaryModel = givenSalaryModel();
		when(salaryRepo.save(salaryModel)).thenReturn(salaryModel);
		assertEquals("saved"+salaryModel.getEmpId(), salaryService.saveSalary(salaryModel));

	}

}
