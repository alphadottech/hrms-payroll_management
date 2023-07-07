package com.adt.payroll.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.TimeSheetModel;


public interface TimeSheetRepo extends JpaRepository<TimeSheetModel, Integer> {

	Optional<TimeSheetModel> findByEmployeeIdAndDate(int id, String localDates);

	TimeSheetModel findByEmployeeId(int id);

	void deleteByEmployeeIdAndDate(int id,String localDates);

	//-----------------------------------------------------------------------------------------------------------------------------
	//	@Query(value="select * from payroll_schema.time_sheet c where c.employee_id=?1 AND c.date BETWEEN ?2 AND ?3",nativeQuery = true )
	//	List<TimeSheetModel> findAllByEmployeeId(int empId, String startDate, String endDate);

	//	@Query(value="select * from payroll_schema.time_sheet c where c.date BETWEEN ?1 AND ?2",nativeQuery = true )
	//	List<TimeSheetModel> findAllByEmployeeId(String startDate, String endDate);

	// JIRA no. - HRMS-88
	List<TimeSheetModel> findAllByEmployeeId(int empId);

	//------------------------------------------------------------------------------------------------------------------------------------
	@Query(value="select * from payroll_schema.time_sheet c where c.date BETWEEN ?1 AND ?2",nativeQuery = true )
	List<TimeSheetModel> findAllByEmployeeId(String startDate, String endDate);

	//	@Query(value = "select * from payroll_schema.time_sheet where (employee_id=?1 and month=?2) and year=?3",nativeQuery = true)
	//	List<TimeSheetModel> search(int id, String month,String year);

	@Query(value = "select * from payroll_schema.time_sheet where (employee_id=?1 and month=?2) and year=?3",nativeQuery = true)
	List<TimeSheetModel> search(int id, String month,String year);

	TimeSheetModel save(Optional<Priortime> priortime);


	@Query(value = "SELECT e.check_in FROM payroll_schema.priortime_table e where employee_id=?1 and date=?2", nativeQuery = true)
	String findCheckInByEmployeeIdAndDate(int empId, String date);


	@Query(value = "SELECT e.check_out FROM payroll_schema.priortime_table e where employee_id=?1 and date=?2", nativeQuery = true)
	String findCheckOutByEmployeeIdAndDate(int empId, String date);

}

