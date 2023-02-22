package com.alphadot.payroll.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.LeaveModel;
import com.alphadot.payroll.repository.LeaveRepository;

@Service
public class LeaveServiceImpl implements LeaveService {




	@Autowired
	private LeaveRepository leaveRepository;
	private static final Logger log=LogManager.getLogger(LeaveServiceImpl.class);
	



	@Override
	public List<LeaveModel> getAllEmpLeave() {
	List<LeaveModel>list=leaveRepository.findAll();
		return list;
	}


	@Override
	public LeaveModel getLeaveById(int id) {
     log.info("Payroll service: LeaveServiceImpl:  getLeaveById Info level log msg");

     LeaveModel leaveModel=leaveRepository.findByEmpId(id);
	
	return leaveModel;
	}



	
}
