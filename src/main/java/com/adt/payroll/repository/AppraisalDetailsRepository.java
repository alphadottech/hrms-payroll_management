package com.adt.payroll.repository;

import com.adt.payroll.model.AppraisalDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AppraisalDetailsRepository extends JpaRepository<AppraisalDetails, Integer> {
}



