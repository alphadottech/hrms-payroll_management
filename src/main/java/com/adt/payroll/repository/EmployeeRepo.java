package com.adt.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adt.payroll.model.Employee;

public interface EmployeeRepo extends JpaRepository<Employee, Integer> {

	Employee findByEmpId(int empId);


}
