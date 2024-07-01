package com.adt.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adt.payroll.model.MonthlySalaryDetails;

import java.util.List;

@Repository
public interface MonthlySalaryDetailsRepo extends JpaRepository<MonthlySalaryDetails, Integer> {
   // List<MonthlySalaryDetails> findByMonthAndYear(String month, String year);

   @Query(value = "SELECT * FROM  payroll_schema.monthly_salary_details" +
           " WHERE salary_month = :month " +
           "AND EXTRACT(YEAR FROM CAST(salary_credited_date AS date)) = :year",
           nativeQuery = true)
   List<MonthlySalaryDetails> findByMonth(@Param("month") String month,@Param("year") int year);
}