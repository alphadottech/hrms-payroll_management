package com.alphadot.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.alphadot.payroll.model.LeaveRequestModel;



public interface LeaveRequestRepo extends JpaRepository<LeaveRequestModel, Integer> {

List<LeaveRequestModel> findByempid(Integer empid);

LeaveRequestModel findByLeaveid(int leaveid);

//@Query(value="SELECT leaveid, empid, status, "
//		+ "leavedate FROM leave_request left join leave_dates on leave_request.leaveid = leave_dates.leave_id "
//		+ "where empid =?1 and leavedate in(:dates)",nativeQuery = true )

@Query(value = "select * from payroll_schema.leave_request where empid =?1 and leaveid =?2",nativeQuery= true)
LeaveRequestModel search(Integer empid, Integer leaveId);

}
