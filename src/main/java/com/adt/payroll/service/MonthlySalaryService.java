package com.adt.payroll.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.adt.payroll.dto.SalaryDTO;
import org.springframework.http.ResponseEntity;

public interface MonthlySalaryService {
    public ResponseEntity<List<SalaryDTO>> getAllMonthlySalaryDetails();

    ByteArrayInputStream getExcelData(Integer empId) throws IOException;
}

