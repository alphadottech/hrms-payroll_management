package com.alphadot.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.alphadot.payroll.model.Priortime;
import com.alphadot.payroll.model.TimeSheetModel;

@Repository
public interface TimeSheetRepo extends JpaRepository<TimeSheetModel, Integer> {

	TimeSheetModel findByEmployeeIdAndDate(int id, String localDates);

	TimeSheetModel findByEmployeeId(int id);

	void deleteByEmployeeIdAndDate(int id, String localDates);

	List<TimeSheetModel> findAllByEmployeeId(int empId);

	@Query(value = "select * from employee_schema.time_sheet where (employee_id=?1 and month=?2) and year=?3", nativeQuery = true)
	List<TimeSheetModel> search(int id, String month, String year);

	TimeSheetModel save(Optional<Priortime> priortime);

	@Query(value = "SELECT e.check_in FROM payroll_schema.priortime_table e where employee_id=?1 and date=?2", nativeQuery = true)
	String findCheckInByEmployeeIdAndDate(int empId, String date);

	@Query(value = "SELECT e.check_out FROM payroll_schema.priortime_table e where employee_id=?1 and date=?2", nativeQuery = true)
	String findCheckOutByEmployeeIdAndDate(int empId, String date);

}
