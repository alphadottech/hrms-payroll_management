package com.adt.payroll.repository;

import java.lang.StackWalker.Option;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adt.payroll.model.ExpenseItems;

public interface ExpenseManagementRepo extends JpaRepository<ExpenseItems, Integer> {
	
	@Query(value = "SELECT * FROM expense_schema.expense_management e WHERE e.employee_id = :empId AND EXTRACT(MONTH FROM payment_date) = :month " 
	+"AND EXTRACT(YEAR FROM payment_date) = :year and status='Approved' ", nativeQuery = true)
	Optional<ExpenseItems> findExpenseDetailsByEmpId(@Param("empId") Integer empId, @Param("month") Integer month, @Param("year") Integer year);

}
