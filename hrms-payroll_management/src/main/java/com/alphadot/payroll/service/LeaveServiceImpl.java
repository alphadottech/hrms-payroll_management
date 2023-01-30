package com.alphadot.payroll.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.LeaveModel;
import com.alphadot.payroll.repository.LeaveRepository;

@Service
public class LeaveServiceImpl implements LeaveService {




	@Autowired
	private LeaveRepository leaveRepository;
	
	
	@Override
	public String saveLeave(LeaveModel leaveModel) {
//		List<Employee> list= employeeRepo.findAllEmployeeId();	
		int empId=leaveModel.getEmpId();
		 	
		LeaveModel op=leaveRepository.findByEmpId(empId);	
	      if(op==null)
	      {
	    	  leaveRepository.save(leaveModel);
	    	  return "Data inserted successfully";			 
	      }
	      return "This employee is already present";	    	  
		}


	@Override
	public List<LeaveModel> getAllEmpLeave() {
	List<LeaveModel>list=leaveRepository.findAll();
		return list;
	}


	@Override
	public String getLeaveById(int id) {
       int leaveCount;
       
       try {
       LeaveModel leaveModel=leaveRepository.findByEmpId(id);
       leaveCount=leaveModel.getLeaveBalance();
	   }
       catch (Exception e){
    	   return "Error in finding employee by this Id";
       }
		return " Total leave: "+leaveCount;
	}


	@Override
	public String updateEmpLeaves(int empId, int leaveCount) {
	       int remainingLeaves;
		   try {
		       LeaveModel leaveModel=leaveRepository.findByEmpId(empId);
		       remainingLeaves=leaveModel.getLeaveBalance()-leaveCount;
		       leaveModel.setLeaveBalance(remainingLeaves);
		       leaveRepository.save(leaveModel);
			   }
		   catch (Exception e){
               return "Unable to fatch employee id or wrong information provided!!";
                }
		return " Remaining leaves"+remainingLeaves;
	}

	
}
