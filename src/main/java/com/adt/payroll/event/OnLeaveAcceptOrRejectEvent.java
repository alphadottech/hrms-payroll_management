package com.adt.payroll.event;

import java.time.Clock;
import java.util.Optional;

import org.springframework.context.ApplicationEvent;

import com.adt.payroll.model.LeaveRequestModel;

import lombok.Data;


public class OnLeaveAcceptOrRejectEvent extends ApplicationEvent{
	
	private Optional<LeaveRequestModel> leaveInfo;

	public Optional<LeaveRequestModel> getLeaveInfo() {
		return leaveInfo;
	}

	public void setLeaveInfo(Optional<LeaveRequestModel> leaveInfo) {
		this.leaveInfo = leaveInfo;
	}

	public OnLeaveAcceptOrRejectEvent(Optional<LeaveRequestModel> leaveInfo) {
		super(leaveInfo);
		this.leaveInfo = leaveInfo;
	}

	
	
	
	

}
