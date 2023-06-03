package com.alphadot.payroll.event.listener;

import java.io.IOException;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.Async;

import com.alphadot.payroll.event.OnPriorTimeAcceptOrRejectEvent;
import com.alphadot.payroll.service.MailService;

import freemarker.template.TemplateException;

public class OnPriorTimeRejectedListener implements ApplicationListener<OnPriorTimeAcceptOrRejectEvent> {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final MailService mailService;

	@Autowired
	public OnPriorTimeRejectedListener(MailService mailService) {
		this.mailService = mailService;
	}

	/**
	 * As soon as a registration event is complete, invoke the email verification
	 * asynchronously in an another thread pool
	 */
	@Override
	@Async
	public void onApplicationEvent(OnPriorTimeAcceptOrRejectEvent onPriortimeApprovalEvent) {
		sendAccountChangeEmailRejected(onPriortimeApprovalEvent);
	}

	/**
	 * Send email verification to the user and persist the token in the database.
	 */
	private void sendAccountChangeEmailRejected(OnPriorTimeAcceptOrRejectEvent event) {
		LOGGER.info("sendAccountChangeEmailRejected");
		String action = event.getAction();
		String actionStatus = event.getActionStatus();
		String recipientAddress = event.getPriortime().get().getEmail();
		try {
			mailService.sendAccountChangeEmail(event, action, actionStatus, recipientAddress);
		} catch (IOException | TemplateException | MessagingException e) {
			throw new MailSendException(recipientAddress);
		}
	}
}
