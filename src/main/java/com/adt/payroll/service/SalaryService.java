package com.adt.payroll.service;

import java.util.List;
import java.util.Optional;

import com.adt.payroll.model.SalaryModel;

public interface SalaryService {

	public List<SalaryModel> getAllEmpSalary();

	public Optional<SalaryModel> getSalaryById(Integer empId);

	public String saveSalary(SalaryModel salaryModel);

	List<SalaryModel> searchByName(String name);
}
