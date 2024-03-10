package com.adt.payroll.repository;

import com.adt.payroll.model.PayRecord;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PayRecordRepo extends JpaRepository<PayRecord, Integer> {

	List<PayRecord> findByEmpId(int empId);
}
