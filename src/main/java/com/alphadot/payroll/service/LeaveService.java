package com.alphadot.payroll.service;

import java.util.List;

import com.alphadot.payroll.model.LeaveModel;

public interface LeaveService {

//

	public List<LeaveModel> getAllEmpLeave();

//
	public LeaveModel getLeaveById(int id);

//
}
