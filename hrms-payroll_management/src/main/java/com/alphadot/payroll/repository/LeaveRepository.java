package com.alphadot.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alphadot.payroll.model.LeaveModel;

public interface LeaveRepository extends JpaRepository<LeaveModel,Integer>{

	LeaveModel findByEmpId(int id);
}
