package com.alphadot.payroll.repository;

import java.util.List;

import javax.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.alphadot.payroll.model.TimeSheetModel;

@Repository
public interface TimeSheetRepo extends JpaRepository<TimeSheetModel, Integer>{

	 TimeSheetModel findByEmployeeIdAndDate(int id, String localDates);

	    TimeSheetModel findByEmployeeId(int id);

	    void deleteByEmployeeIdAndDate(int id,String localDates);

		List<TimeSheetModel> findAllByEmployeeId(int empId);

		@Query(value="select * from payroll_schema.time_sheet c where c.employee_id=?1 AND c.date BETWEEN ?2 AND ?3",nativeQuery = true )
		List<TimeSheetModel> findAllByEmployeeId(int empId, String startDate, String endDate);

		
		@Query(value="select * from payroll_schema.time_sheet c where c.date BETWEEN ?1 AND ?2",nativeQuery = true )
		List<TimeSheetModel> findAllByEmployeeId(String startDate, String endDate);

		
		@Query(value = "select * from payroll_schema.time_sheet where (employee_id=?1 and month=?2) and year=?3",nativeQuery = true)
	      List<TimeSheetModel> search(int id, String month,String year);

		
	}
