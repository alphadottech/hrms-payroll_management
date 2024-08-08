package com.adt.payroll.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;

import com.adt.payroll.model.LeaveRequestModel;
import freemarker.template.TemplateException;

import jakarta.mail.MessagingException;


public interface LeaveRequestService {

	public String saveLeaveRequest(LeaveRequestModel lr);

	public List<LeaveRequestModel> getLeaveDetails();

	public Page<LeaveRequestModel> getLeaveRequestDetailsByEmpId(int page, int size,Integer empid);

	public String AcceptLeaveRequest(Integer empid, Integer leaveId,Integer leaveDate ,String leavetype, String leaveReason) throws TemplateException, MessagingException, IOException;
	
	public String RejectLeaveRequest(Integer empid, Integer leaveId,String leavetype, String leaveReason) throws TemplateException, MessagingException, IOException;

	public List<LeaveRequestModel> getAllEmployeeLeaveDetails();
	
    public	String reSendLeaveRequest(int leaveId);
	LeaveRequestModel getLeaveRequestDetailsByEmpIdAndLeaveId(int empId, int leaveId);



}