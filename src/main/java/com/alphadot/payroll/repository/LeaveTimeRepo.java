package com.alphadot.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alphadot.payroll.model.LeaveTime;


public interface LeaveTimeRepo extends JpaRepository<LeaveTime, Integer>{
//	@Query(value = "select * from employee_schema.leave_time where (emp_id=?1 and month=?2",nativeQuery = true)

	List<LeaveTime> findByEmpIdAndMonth(int id, String month);

}
