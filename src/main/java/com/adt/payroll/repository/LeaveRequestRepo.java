package com.adt.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.adt.payroll.model.LeaveRequestModel;



public interface LeaveRequestRepo extends JpaRepository<LeaveRequestModel, Integer> {

List<LeaveRequestModel> findByempid(Integer empid);

Page<LeaveRequestModel> findByempid(Integer empid,Pageable pageable);

LeaveRequestModel findByLeaveid(int leaveid);

@Query(value = "select * from payroll_schema.leave_request where empid =?1 and leaveid =?2",nativeQuery= true)
LeaveRequestModel search(Integer empid, Integer leaveId);

    Optional<Object> findByEmpidAndLeaveid(int empId, int leaveId);
}
