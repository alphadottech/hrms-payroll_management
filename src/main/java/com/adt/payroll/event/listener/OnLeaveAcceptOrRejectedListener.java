package com.adt.payroll.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.adt.payroll.event.OnLeaveAcceptOrRejectEvent;
import com.adt.payroll.event.OnPriorTimeAcceptOrRejectEvent;
import com.adt.payroll.service.CommonEmailService;

@Component
public class OnLeaveAcceptOrRejectedListener  implements ApplicationListener<OnLeaveAcceptOrRejectEvent> {

	
	private CommonEmailService emailService;

	@Autowired
	public OnLeaveAcceptOrRejectedListener(CommonEmailService emailService) {
		this.emailService = emailService;
	}

	/**
	 * As soon as a registration event is complete, invoke the email verification
	 * asynchronously in an another thread pool
	 */
	@Override
	@Async
	public void onApplicationEvent(OnLeaveAcceptOrRejectEvent onLeaveApprovalOrRejectEvent) {
//		sendAccountChangeEmailRejected(onPriortimeApprovalEvent);
		emailService.sendLeaveAcceptAndRejectedEmail(onLeaveApprovalOrRejectEvent);
	}
	
}
