package com.alphadot.payroll.service;

import java.util.List;


import com.alphadot.payroll.model.SalaryModel;

public interface SalaryService {

	public List<SalaryModel> getAllEmpSalary();
	
	public String workingDays(int empId);
}
