package com.alphadot.payroll.model;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alphadot.payroll.service.EmailService;

import freemarker.template.TemplateException;

@Component
public class OnLeaveRequestSaveEventListener implements ApplicationListener<OnLeaveRequestSaveEvent> {
	
	private EmailService mailService;
	
	
	@Autowired
	public OnLeaveRequestSaveEventListener(EmailService mailService) {
		this.mailService = mailService;
	}



	@Override
	@Async
	public void onApplicationEvent(OnLeaveRequestSaveEvent event) {
		sendEmail(event);
	}



	private void sendEmail(OnLeaveRequestSaveEvent event) {
		LeaveRequestModel leaveRequestModel = event.getLeaveRequestModel();
		String emailApprovalUrl = event.getRedirectUrl().toUriString();
		String emailRejectionUrl = event.getRedirectUrl1().toUriString();

		try {
			mailService.sendEmail(event,emailApprovalUrl, emailRejectionUrl, leaveRequestModel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
