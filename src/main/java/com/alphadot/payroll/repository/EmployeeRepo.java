package com.alphadot.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alphadot.payroll.model.Employee;

public interface EmployeeRepo extends JpaRepository<Employee, Integer> {

	Employee findByEmpId(int empId);


}
