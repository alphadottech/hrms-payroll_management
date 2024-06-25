package com.adt.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adt.payroll.model.MonthlySalaryDetails;

@Repository
public interface MonthlySalaryDetailsRepo extends JpaRepository<MonthlySalaryDetails, Integer> {

}