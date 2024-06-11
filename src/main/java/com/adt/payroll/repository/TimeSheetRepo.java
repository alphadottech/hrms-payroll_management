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
	
	@Query(value="select * from payroll_schema.time_sheet c where c.employee_id =?1 and to_date(c.date, 'dd-mm-yyyy') BETWEEN to_date(?2, 'DD-MM-YYYY') and to_date(?3, 'DD-MM-YYYY');",nativeQuery = true )
	List<TimeSheetModel> findAllByEmployeeIdWithinSpecifiedDateRange(int empId, String startDate, String endDate);

	@Query(value = "SELECT * FROM payroll_schema.time_sheet c \n" +
			"WHERE c.date ~ '^\\d{2}-\\d{2}-\\d' AND TO_DATE(c.date, 'dd-MM-yyyy') BETWEEN TO_DATE(?1, 'dd-MM-yyyy') AND TO_DATE(?2, 'dd-MM-yyyy')\n", nativeQuery = true)
	List<TimeSheetModel> findAllWithinSpecifiedDateRange(String startDate, String endDate);

	@Query(value="select * from payroll_schema.time_sheet c where c.date BETWEEN ?1 AND ?2",nativeQuery = true )
	List<TimeSheetModel> findAllByEmployeeId(String startDate, String endDate);


	@Query(value = "select * from payroll_schema.time_sheet where (employee_id=?1 and month=?2) and year=?3",nativeQuery = true)
	List<TimeSheetModel> search(int id, String month,String year);

	TimeSheetModel save(Optional<Priortime> priortime);


	@Query(value = "SELECT e.check_in FROM payroll_schema.priortime_table e where employee_id=?1 and date=?2", nativeQuery = true)
	String findCheckInByEmployeeIdAndDate(int empId, String date);


	@Query(value = "SELECT e.check_out FROM payroll_schema.priortime_table e where employee_id=?1 and date=?2", nativeQuery = true)
	String findCheckOutByEmployeeIdAndDate(int empId, String date);

	@Query(value = "SELECT count(*) as empTotalWorkingDay FROM payroll_schema.time_sheet where employee_id=?1 and month=?2 and year=?3 and status='Present' and working_hour>='6:00:00'", nativeQuery = true)
	public int findEmpTotalWorkingDayCount(int empId, String month, String year);

	@Query(value = "SELECT count(*) as empHalfDay FROM payroll_schema.time_sheet where employee_id=?1 and month=?2 and year=?3 and status='Present' and working_hour<'6:00:00'", nativeQuery = true)
	public int findEmpTotalHalfDayCount(int empId, String month, String year);
	
    @Query(value = "SELECT * FROM payroll_schema.Time_sheet t WHERE  t.month=?1 and t.year=?2 and (t.check_in IS NULL OR t.check_out IS NULL OR (t.working_hour IS NULL OR t.working_hour<'9:30:00'))", nativeQuery = true)
	public List<TimeSheetModel> findTimeSheetWithNullValues(String month, String year);
}

