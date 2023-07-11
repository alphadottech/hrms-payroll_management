package com.adt.payroll.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

import com.adt.payroll.event.OnEmployeeExpenseAcceptOrRejectEvent;
import com.adt.payroll.event.OnEmployeeExpenseDetailsSavedEvent;
import com.adt.payroll.event.OnPriorTimeAcceptOrRejectEvent;
import com.adt.payroll.event.OnPriorTimeDetailsSavedEvent;
import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.model.Mail;
import com.adt.payroll.model.OnLeaveRequestSaveEvent;

import freemarker.template.TemplateException;

public interface CommonEmailService {


	//START:- For Prior Time
	public void sendEmailVerification(OnPriorTimeDetailsSavedEvent event);

	public void sendEmailVerification(OnPriorTimeDetailsSavedEvent event, String emailVerificationUrl1, 
			String emailVerificationUrl2, String to) throws IOException, TemplateException, MessagingException;

	public void sendAccountChangeEmail(OnPriorTimeAcceptOrRejectEvent event,String action, String actionStatus, String to) throws IOException, TemplateException, MessagingException;

	public void send(Mail mail) throws MessagingException, UnsupportedEncodingException;
	
	public void sendAccountChangeEmailRejected(OnPriorTimeAcceptOrRejectEvent event);
	//END:- For Prior Time

	//*** Send email after generating PaySlip ***
	public void sendEmail(ByteArrayOutputStream baos, String name, String gmail, String monthYear);

	//*** START:- Send Email for Leave Request
	public String sendEmail(OnLeaveRequestSaveEvent event, String Url, String Url1, LeaveRequestModel lr) throws IOException, TemplateException, MessagingException;

	public void sendEmail(OnLeaveRequestSaveEvent event);
	//*** END:- Send Email for Leave Request
	
	public void sendEmployeeExpenseVerification(OnEmployeeExpenseDetailsSavedEvent event, String emailVerificationUrl1, String to) throws IOException, TemplateException, MessagingException;
	 
	public void sendEmployeeExpenseApprovalEmail(OnEmployeeExpenseAcceptOrRejectEvent event, String action, String actionStatus, String to) throws IOException, TemplateException, MessagingException;
	
	public void sendAccountChangeEmailApproved(OnEmployeeExpenseAcceptOrRejectEvent event) throws IOException, TemplateException, MessagingException;



}
