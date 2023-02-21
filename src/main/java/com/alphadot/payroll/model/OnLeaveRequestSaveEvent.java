package com.alphadot.payroll.model;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.util.UriComponentsBuilder;

public class OnLeaveRequestSaveEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	private transient UriComponentsBuilder redirectUrl;
	private transient UriComponentsBuilder redirectUrl1;
	private LeaveRequestModel leaveRequestModel;
	public OnLeaveRequestSaveEvent(UriComponentsBuilder redirectUrl, UriComponentsBuilder redirectUrl1,
			LeaveRequestModel leaveRequestModel) {
		super(leaveRequestModel);
		this.redirectUrl = redirectUrl;
		this.redirectUrl1 = redirectUrl1;
		this.leaveRequestModel = leaveRequestModel;
	}
	public UriComponentsBuilder getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(UriComponentsBuilder redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	public UriComponentsBuilder getRedirectUrl1() {
		return redirectUrl1;
	}
	public void setRedirectUrl1(UriComponentsBuilder redirectUrl1) {
		this.redirectUrl1 = redirectUrl1;
	}
	public LeaveRequestModel getLeaveRequestModel() {
		return leaveRequestModel;
	}
	public void setLeaveRequestModel(LeaveRequestModel leaveRequestModel) {
		this.leaveRequestModel = leaveRequestModel;
	}

	
	
	

}
