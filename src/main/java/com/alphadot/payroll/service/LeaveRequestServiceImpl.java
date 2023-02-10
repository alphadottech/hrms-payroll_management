package com.alphadot.payroll.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.Employee;
import com.alphadot.payroll.model.LeaveRequestModel;
import com.alphadot.payroll.repository.EmployeeRepo;
import com.alphadot.payroll.repository.LeaveRequestRepo;



@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

	@Autowired
	private LeaveRequestRepo leaveRequestRepo;

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private EmployeeRepo employeeRepo;

	@Override
	public String saveLeaveRequest(LeaveRequestModel lr) {

		List<LeaveRequestModel> opt = leaveRequestRepo.findByempid(lr.getEmpid());

		int counter = 0;
		if (!opt.isEmpty()) {
			for (LeaveRequestModel lrm : opt) {
				List<String> dbld = lrm.getLeavedate();
				List<String> uild = lr.getLeavedate();
				for (String tempLd : uild) {
					if (dbld.contains(tempLd)) {
						counter++;
					}
				}
			}
			if (counter == 0) {
				List<String> li = lr.getLeavedate();
				lr.setLeavedate(li);
				lr.setStatus("Pending");

				leaveRequestRepo.save(lr);

				Employee employee = employeeRepo.findByEmpId(lr.getEmpid());
				String email = employee.getEmailId();
				emailService.sendEmail(email);

			} else {
				return "you have selected wrong date OR already requested for selected date";
			}
		} else {
			lr.setStatus("Pending");
			leaveRequestRepo.save(lr);
		}

		return lr.getLeaveid() + " Leave Request is saved & mail Sent Successfully";

	}

	@Override
	public List<LeaveRequestModel> getLeaveDetails() {
		List<LeaveRequestModel> leavelist = leaveRequestRepo.findAll();
		return leavelist;
	}

	@Override
	public List<LeaveRequestModel> getLeaveRequestDetailsByEmpId(Integer empid) {
		List<LeaveRequestModel> opt = leaveRequestRepo.findByempid(empid);
		if(!opt.isEmpty()) {
			return opt;
		} else {
			return null;
		}

	}

}