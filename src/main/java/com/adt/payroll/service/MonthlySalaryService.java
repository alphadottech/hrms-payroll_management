package com.adt.payroll.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.adt.payroll.dto.SalaryDTO;

public interface MonthlySalaryService {
	 List<SalaryDTO> getAllMonthlySalaryDetails();

    ByteArrayInputStream getExcelData(Integer empId) throws IOException;
}

