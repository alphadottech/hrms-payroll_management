package com.adt.payroll.service;

import com.adt.payroll.dto.MonthSalaryDTO;
import com.adt.payroll.model.MonthlySalaryDetails;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MonthlySalaryService {
	 List<MonthSalaryDTO> getAllMonthlySalaryDetails();
   
}

