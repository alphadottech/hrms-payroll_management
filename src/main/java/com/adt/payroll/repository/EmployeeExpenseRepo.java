package com.adt.payroll.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.adt.payroll.model.EmployeeExpense;

public interface EmployeeExpenseRepo extends JpaRepository<EmployeeExpense, Integer> {
}
