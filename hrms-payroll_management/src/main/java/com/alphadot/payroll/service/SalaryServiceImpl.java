package com.alphadot.payroll.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.model.SalaryModel;
import com.alphadot.payroll.repository.TimeSheetRepo;
import com.alphadot.payroll.repository.SalaryRepo;

@Service
public class SalaryServiceImpl implements SalaryService {

	@Autowired
	private SalaryRepo salaryRepo;
	
	@Autowired
	private TimeSheetRepo timeSheetRepo;
	
	
	@Override
	public List<SalaryModel> getAllEmpSalary() {
	List<SalaryModel> list=salaryRepo.findAll();
		
		return list;
	}

	public String workingDays(int empId) {
      List<TimeSheetModel> li=timeSheetRepo.findAllByEmployeeId(empId);
     
      int dayCount; 
      if(li.isEmpty()) {
    	   return "No employee data is available with this id";
       }  
       else {
    	   dayCount=li.size();
    	   return " Working days: "+dayCount;
       }
		
	}
	
}
