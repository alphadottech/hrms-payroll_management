package com.alphadot.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alphadot.payroll.model.SalaryModel;

@Repository
public interface SalaryRepo extends JpaRepository<SalaryModel, Integer>{

}
