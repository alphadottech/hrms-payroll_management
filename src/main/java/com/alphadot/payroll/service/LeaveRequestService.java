package com.alphadot.payroll.service;

import java.util.List;

import com.alphadot.payroll.model.LeaveRequestModel;



public interface LeaveRequestService {

	public String saveLeaveRequest(LeaveRequestModel lr);

	public List<LeaveRequestModel> getLeaveDetails();

	public List<LeaveRequestModel> getLeaveRequestDetailsByEmpId(Integer empid);

	public String AcceptLeaveRequest(Integer empid, Integer leaveId);
	
	public String RejectLeaveRequest(Integer empid, Integer leaveId);

}