package com.alphadot.payroll.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.LeaveModel;
import com.alphadot.payroll.repository.LeaveRepository;

@Service
public class LeaveServiceImpl implements LeaveService {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LeaveRepository leaveRepository;

	@Override
	public List<LeaveModel> getAllEmpLeave() {
		List<LeaveModel> list = leaveRepository.findAll();
		return list;
	}

	@Override
	public LeaveModel getLeaveById(int id) {
		LOGGER.info("Payroll service: LeaveServiceImpl:  getLeaveById Info level log msg");
		LeaveModel leaveModel = leaveRepository.findByEmpId(id);
		return leaveModel;
	}

}
