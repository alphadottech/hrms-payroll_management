package com.adt.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adt.payroll.model.MonthlySalaryDetails;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlySalaryDetailsRepo extends JpaRepository<MonthlySalaryDetails, Integer> {

   @Query(value = "SELECT * FROM  payroll_schema.monthly_salary_details" +
           " WHERE salary_month = :month " +
           "AND EXTRACT(YEAR FROM CAST(salary_credited_date AS date)) = :year",
           nativeQuery = true)
   List<MonthlySalaryDetails> findByMonth(@Param("month") String month,@Param("year") int year);
    
   Optional<List<MonthlySalaryDetails>> findByCreditedDate(String date);
  


    @Query(value = "SELECT * FROM payroll_schema.monthly_salary_details WHERE emp_id = :empId", nativeQuery = true)
    List<MonthlySalaryDetails> findSalaryDetailsByEmpId(@Param("empId") Integer empId);
    
	
	  @Query(value="SELECT MAX(msd.salary_credited_date) FROM payroll_schema.monthly_salary_details msd",nativeQuery = true)
	  String findLatestSalaryCreditedDate();
	 
	  @Query(value="SELECT MAX(msd.updated_When) FROM payroll_schema.monthly_salary_details msd",nativeQuery = true)
	  Timestamp findLatestSalaryUpdatedDate();

}