package com.adt.payroll.service;

import java.util.List;

import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.repository.LeaveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.model.OnLeaveRequestSaveEvent;
import com.adt.payroll.repository.LeaveRequestRepo;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private LeaveRequestRepo leaveRequestRepo;

	@Autowired
	private LeaveRepository leaveRepository;

	private final ApplicationEventPublisher applicationEventPublisher;

	public LeaveRequestServiceImpl(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public String saveLeaveRequest(LeaveRequestModel lr) {

		LOGGER.info("Payroll service: LeaveRequestServiceImpl:  saveLeaveRequest Info level log msg");

		List<LeaveRequestModel> opt = leaveRequestRepo.findByempid(lr.getEmpid());
		int counter = 0;
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
			//List<String> li = lr.getLeavedate();
			//	lr.setLeavedate(li);
			lr.setStatus("Pending");

			leaveRequestRepo.save(lr);
			int id = lr.getEmpid();
			int leaveId = lr.getLeaveid();
			UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/leave/leave/Accepted/" + id + "/" + leaveId+ "/"+lr.getLeavedate().size());
			UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/leave/leave/Rejected/" + id + "/" + leaveId);
			
			System.out.println(urlBuilder.toUriString());
			System.out.println(urlBuilder1.toUriString());

			//				UriComponentsBuilder urlBuilder2 = ServletUriComponentsBuilder.fromHttpUrl("http://localhost:9095/payroll/leave/leave/Rejected/"+id+"/"+leaveId);

			OnLeaveRequestSaveEvent onLeaveRequestSaveEvent = new OnLeaveRequestSaveEvent(urlBuilder, urlBuilder1, lr);
			applicationEventPublisher.publishEvent(onLeaveRequestSaveEvent);

		} else {
			return "you have selected wrong date OR already requested for selected date";
		}


		return lr.getLeaveid() + " Leave Request is saved & mail Sent Successfully";
	}

	@Override
	public List<LeaveRequestModel> getLeaveDetails() {
		LOGGER.info("Payroll service: LeaveRequestServiceImpl:  getLeaveDetails Info level log msg");
		List<LeaveRequestModel> leavelist = leaveRequestRepo.findAll();
		return leavelist;
	}

	@Override
	public List<LeaveRequestModel> getLeaveRequestDetailsByEmpId(Integer empid) {
		LOGGER.info("Payroll service: LeaveRequestServiceImpl:  getLeaveRequestDetailsByEmpId Info level log msg");
		List<LeaveRequestModel> opt = leaveRequestRepo.findByempid(empid);
		if (!opt.isEmpty()) {
			return opt;
		} else {
			return null;
		}

	}

	@Override
	public String AcceptLeaveRequest(Integer empid, Integer leaveId,Integer leaveDate) {

		LeaveRequestModel opt = leaveRequestRepo.search(empid, leaveId);

		if (opt != null && opt.getStatus().equalsIgnoreCase("Pending")) {
			LeaveModel leaveModel = leaveRepository.findByEmpId(empid);

			if(leaveModel.getLeaveBalance()>=leaveDate) {
				opt.setStatus("Accepted");
				leaveRequestRepo.save(opt);
				leaveModel.setLeaveBalance(leaveModel.getLeaveBalance() - leaveDate);
			}
			else{
				return "You dont have sufficient Leave Balance";
			}
			leaveRepository.save(leaveModel);
			return opt.getLeaveid() + " leave Request Accepted";
		} else {
			return empid + "leave request status already updated";
		}

	}

	@Override
	public String RejectLeaveRequest(Integer empid, Integer leaveId) {
		LeaveRequestModel opt = leaveRequestRepo.search(empid, leaveId);

		if (opt != null && opt.getStatus().equalsIgnoreCase("Pending")) {
			opt.setStatus("Rejected");
			leaveRequestRepo.save(opt);
			return opt.getLeaveid() + " leave Request Rejected";
		} else {
			return empid + " leave request status already updated";
		}
	}

}