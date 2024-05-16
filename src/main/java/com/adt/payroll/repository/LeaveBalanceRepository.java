package com.adt.payroll.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.adt.payroll.model.LeaveBalance;

import jakarta.transaction.Transactional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Integer> {

	LeaveBalance findByEmpId(int id);

	@Query(value = "select * from payroll_schema.leave_balance where emp_id=?1", nativeQuery = true)
	Optional<LeaveBalance> findByEmployeeId(int empId);

	@Transactional
	@Modifying
	@Query(value = "UPDATE payroll_schema.leave_balance SET leave_balance=?2, paid_leave=?3, unpaid_leave=?4, half_day=?5 WHERE emp_id =?1", nativeQuery = true)
	public void updateAllLeavesByEmpId( int empId,int leaveBal, int empPaidLeave, int empUnpaidLeave, int halfday);

//UPDATE payroll_schema.leave_balance SET leave_balance=0, paid_leave=1, unpaid_leave=2 WHERE emp_id ='37'
	// half_day=1,

}
