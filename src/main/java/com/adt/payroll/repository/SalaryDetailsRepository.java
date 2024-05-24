package com.adt.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.adt.payroll.model.SalaryDetails;
import com.adt.payroll.model.SalaryModel;

import jakarta.transaction.Transactional;

@Repository
public interface SalaryDetailsRepository extends JpaRepository<SalaryDetails, Integer> {

	@Query(value = "SELECT * FROM payroll_schema.salary_details WHERE emp_id=?1", nativeQuery = true)
	Optional<SalaryDetails> findByEmployeeId(int empId);

//	  Optional<SalaryDetails> findByEmployeeId(int empId);

//	@Transactional
//	@Modifying
//	@Query(value = "UPDATE payroll_schema.salary_details SET gross_salary=?1, net_salary=?2 WHERE emp_id=?3", nativeQuery = true)
//	public void updateGrossAndNetSalaryByEmpId(double grossSalary, double netAmount, int empId);

//	@Transactional
//	@Modifying
//	@Query(value = "UPDATE payroll_schema.salary_details SET absent_deduction=?2 WHERE emp_id=?1", nativeQuery = true)
//	public void updateAbsentDeductionAmount(int empId, double absentDeduction);

//	@Query(value = "select absent_deduction from payroll_schema.salary_details WHERE emp_id=?1", nativeQuery = true)
//	public double findAbsentDeductionbyEmpId(int empId);

//	@Query(value = "SELECT * FROM payroll_schema.salary_table  WHERE employee LIKE %?1%", nativeQuery = true)
//	List<SalaryModel> searchByEmpName(String name);

}
