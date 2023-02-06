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


	
	@Query(value = "select * from employee_schema.time_sheet where (employee_id=?1 and month=?2) and year=?3",nativeQuery = true)
List<TimeSheetModel> search(int id, String month,String year);




}
