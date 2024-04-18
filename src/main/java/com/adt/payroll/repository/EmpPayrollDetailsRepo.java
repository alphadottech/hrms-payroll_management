package com.adt.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adt.payroll.model.EmpPayrollDetails;
import com.adt.payroll.model.EmployeeExpense;

public interface EmpPayrollDetailsRepo extends JpaRepository<EmpPayrollDetails, Integer>  {
	
	EmpPayrollDetails getByEmpId(int empId);
	

}
