package com.adt.payroll.repository;

import com.adt.payroll.model.PayRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayRecordRepo extends JpaRepository<PayRecord,Integer> {

    PayRecord findByEmpId(int empId);
}
