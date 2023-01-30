package com.alphadot.payroll.service;

import java.util.List;

import com.alphadot.payroll.model.LeaveModel;

public interface LeaveService {

	public String saveLeave(LeaveModel leaveModel);

	public List<LeaveModel> getAllEmpLeave();

	public String getLeaveById(int id);

	public String updateEmpLeaves(int empId, int leaveCount);
}
