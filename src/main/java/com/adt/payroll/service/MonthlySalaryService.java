package com.adt.payroll.service;

import com.adt.payroll.model.MonthlySalaryDetails;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MonthlySalaryService {
    ResponseEntity<Object> getAllMonthlySalaryDetails();
}

