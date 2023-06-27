package com.adt.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.adt.payroll.model.SalaryModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepo extends JpaRepository<SalaryModel, Integer> {

    //	@Query(value = "SELECT  e.designation, e.dob,  e.first_name, e.gender, e.last_name,  e.mobile_no,e.  e.salary FROM user_schema._employee e where e.employee_id=:?,", nativeQuery=true)
//	User findSubDetail(@Param("e.employee_id") int empId);
    Optional<SalaryModel> findByEmpId(Integer empId);
    @Query(value = "SELECT * FROM payroll_schema.salary_table  WHERE employee LIKE %?1%",nativeQuery = true)
    List<SalaryModel> searchByEmpName(String name);
}
