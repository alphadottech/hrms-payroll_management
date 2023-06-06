package com.adt.payroll.service;

import java.util.List;

import com.adt.payroll.model.LeaveModel;

public interface LeaveService {

//

	public List<LeaveModel> getAllEmpLeave();

//
	public LeaveModel getLeaveById(int id);

//
}
