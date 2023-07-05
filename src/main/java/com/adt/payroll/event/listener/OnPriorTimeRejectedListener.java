package com.adt.payroll.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.adt.payroll.event.OnPriorTimeAcceptOrRejectEvent;
import com.adt.payroll.service.CommonEmailService;

@Component
public class OnPriorTimeRejectedListener implements ApplicationListener<OnPriorTimeAcceptOrRejectEvent> {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	//private final MailService mailService;
	
	private CommonEmailService emailService;

	@Autowired
	public OnPriorTimeRejectedListener(CommonEmailService emailService) {
		this.emailService = emailService;
	}

	/**
	 * As soon as a registration event is complete, invoke the email verification
	 * asynchronously in an another thread pool
	 */
	@Override
	@Async
	public void onApplicationEvent(OnPriorTimeAcceptOrRejectEvent onPriortimeApprovalEvent) {
//		sendAccountChangeEmailRejected(onPriortimeApprovalEvent);
		emailService.sendAccountChangeEmailRejected(onPriortimeApprovalEvent);
	}

	/**
	 * Send email verification to the user and persist the token in the database.
	 */
//	private void sendAccountChangeEmailRejected(OnPriorTimeAcceptOrRejectEvent event) {
//		LOGGER.info("sendAccountChangeEmailRejected");
//		String action = event.getAction();
//		String actionStatus = event.getActionStatus();
//		String recipientAddress = event.getPriortime().get().getEmail();
//		try {
//			mailService.sendAccountChangeEmail(event, action, actionStatus, recipientAddress);
//		} catch (IOException | TemplateException | MessagingException e) {
//			throw new MailSendException(recipientAddress);
//		}
//	}
}
