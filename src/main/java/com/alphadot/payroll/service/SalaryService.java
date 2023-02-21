package com.alphadot.payroll.service;

import java.util.List;
import java.util.Optional;

import com.alphadot.payroll.model.SalaryModel;

public interface SalaryService {

	public List<SalaryModel> getAllEmpSalary();
	
	public Optional<SalaryModel> getSalaryById(int empId);
}
