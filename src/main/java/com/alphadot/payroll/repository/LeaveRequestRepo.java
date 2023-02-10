package com.alphadot.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alphadot.payroll.model.LeaveRequestModel;



public interface LeaveRequestRepo extends JpaRepository<LeaveRequestModel, Integer> {

List<LeaveRequestModel> findByempid(Integer empid);

}
