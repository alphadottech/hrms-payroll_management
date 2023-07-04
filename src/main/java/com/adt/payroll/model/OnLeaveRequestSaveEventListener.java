package com.adt.payroll.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.adt.payroll.service.CommonEmailService;

@Component
public class OnLeaveRequestSaveEventListener implements ApplicationListener<OnLeaveRequestSaveEvent> {
	
	//private EmailService mailService;
	
	private CommonEmailService emailService;
	
	@Autowired
	public OnLeaveRequestSaveEventListener(CommonEmailService emailService) {
		this.emailService = emailService;
	}

	@Override
	@Async
	public void onApplicationEvent(OnLeaveRequestSaveEvent event) {
		emailService.sendEmail(event);
	}

//	private void sendEmail(OnLeaveRequestSaveEvent event) {
//		LeaveRequestModel leaveRequestModel = event.getLeaveRequestModel();
//        String emailApprovalUrl = event.getRedirectUrl().toUriString();
//		String emailRejectionUrl = event.getRedirectUrl1().toUriString();
//
//		try {
//			mailService.sendEmail(event, emailApprovalUrl, emailRejectionUrl, leaveRequestModel);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (TemplateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (MessagingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//	}

}
