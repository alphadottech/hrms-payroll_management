package com.adt.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adt.payroll.model.LeaveModel;

public interface LeaveRepository extends JpaRepository<LeaveModel,Integer>{

	LeaveModel findByEmpId(int id);
}
