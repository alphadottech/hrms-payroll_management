package com.alphadot.payroll.service;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.SalaryModel;
import com.alphadot.payroll.repository.SalaryRepo;

@Service
public class SalaryServiceImpl implements SalaryService {

	@Autowired
	private SalaryRepo salaryRepo;
	
	
	private static final Logger log=LogManager.getLogger(SalaryServiceImpl.class);
	

	@Override
	public List<SalaryModel> getAllEmpSalary() {
	List<SalaryModel> list=salaryRepo.findAll();
	log.info("Payroll service: SalaryServiceImpl:  getAllEmpSalary Info level log msg");
		
		return list;
	}

	public Optional<SalaryModel> getSalaryById(int empId) {
		log.info("Payroll service: SalaryServiceImpl:  getSalaryById Info level log msg");

		Optional<SalaryModel> model= salaryRepo.findById(empId);
                
      if(model==null) {
    	   throw new NullPointerException("No Data exist with given ID");
       }  
       else {
    	   return model;
       }
		
	}
	
}
