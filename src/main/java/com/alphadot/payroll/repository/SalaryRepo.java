package com.alphadot.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alphadot.payroll.model.SalaryModel;

@Repository
public interface SalaryRepo extends JpaRepository<SalaryModel, Integer>{

//	@Query(value = "SELECT  e.designation, e.dob,  e.first_name, e.gender, e.last_name,  e.mobile_no,e.  e.salary FROM user_schema._employee e where e.employee_id=:?,", nativeQuery=true)
//	User findSubDetail(@Param("e.employee_id") int empId);
}
