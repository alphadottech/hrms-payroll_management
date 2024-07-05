package com.adt.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.adt.payroll.model.EmpPayrollDetails;
import com.adt.payroll.model.EmployeeExpense;

public interface EmpPayrollDetailsRepo extends JpaRepository<EmpPayrollDetails, Integer>  {
	
	EmpPayrollDetails getByEmpId(int empId);
	
	@Query(value = "SELECT * FROM payroll_schema.emp_payroll_details where emp_id=?1", nativeQuery = true)
	Optional<EmpPayrollDetails> findByEmployeeId(int empId);




//    @Query(value = "select * from payroll_schema.emp_payroll_details where emp_id=?1", nativeQuery = true)
//    Optional<EmpPayrollDetails> findByEmployeeId(Integer empId);
}
