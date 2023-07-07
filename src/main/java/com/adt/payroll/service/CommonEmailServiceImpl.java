package com.adt.payroll.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.adt.payroll.dto.EmployeeExpenseDTO;
import com.adt.payroll.event.OnEmployeeExpenseAcceptOrRejectEvent;
import com.adt.payroll.event.OnEmployeeExpenseDetailsSavedEvent;
import com.adt.payroll.event.OnPriorTimeAcceptOrRejectEvent;
import com.adt.payroll.event.OnPriorTimeDetailsSavedEvent;
import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.model.Mail;
import com.adt.payroll.model.OnLeaveRequestSaveEvent;
import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.UserRepo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Service
public class CommonEmailServiceImpl implements CommonEmailService {

	private static final Logger log = LogManager.getLogger(PayRollServiceImpl.class);

	@Value("${app.velocity.templates.location}")
	private String basePackagePath;

	@Value("${spring.mail.username}")
	private String sender;
	
	@Value("${spring.mail.username}")
	private String mailFrom;

	private Configuration templateConfiguration;

	@Autowired
	private final JavaMailSender mailSender;

	@Autowired
	private UserRepo userRepo;

	public CommonEmailServiceImpl(JavaMailSender mailSender, Configuration templateConfiguration) {
		this.mailSender = mailSender;
		this.templateConfiguration = templateConfiguration;
	}

	//*** START:- For Prior Time ***
	/**
	 * Send email verification to the user and persist the token in the database.
	 */
	@Override
	public void sendEmailVerification(OnPriorTimeDetailsSavedEvent event) {
		Priortime priortime = event.getPriorTime();
		String recipientAddress = priortime.getEmail();
		String emailConfirmationUrl1 = event.getRedirectUrl1().toUriString();
		String emailConfirmationUrl2 = event.getRedirectUrl2().toUriString();


		try {
			sendEmailVerification(event, emailConfirmationUrl1, emailConfirmationUrl2, recipientAddress);
		} catch (IOException | TemplateException | MessagingException e) {
			throw new MailSendException(recipientAddress);
		}
	}
	
	/**
	 * Send email verification to the user and persist the token in the database.
	 */
	@Override
	public void sendAccountChangeEmailRejected(OnPriorTimeAcceptOrRejectEvent event) {
		log.info("sendAccountChangeEmailRejected");
		String action = event.getAction();
		String actionStatus = event.getActionStatus();
		String recipientAddress = event.getPriortime().get().getEmail();
		try {
			sendAccountChangeEmail(event, action, actionStatus, recipientAddress);
		} catch (IOException | TemplateException | MessagingException e) {
			throw new MailSendException(recipientAddress);
		}
	}

	@Override
	public void sendEmailVerification(OnPriorTimeDetailsSavedEvent event, String emailVerificationUrl1,
			String emailVerificationUrl2, String to) throws IOException, TemplateException, MessagingException {

		Mail mail = new Mail();
		mail.setSubject("Email Verification [Team CEP]");
		mail.setTo(to);
		mail.setFrom(sender);
		mail.getModel().put("userName", to);
		mail.getModel().put("approveLeaveRequestLink1", emailVerificationUrl1);
		mail.getModel().put("RejectLeaveRequestLink2", emailVerificationUrl2);
		mail.getModel().put("Email", event.getPriorTime().getEmail());
		mail.getModel().put("CheckInTime", event.getPriorTime().getCheckIn());
		mail.getModel().put("CheckOutTime", event.getPriorTime().getCheckOut());
		mail.getModel().put("Date", event.getPriorTime().getDate());
		mail.getModel().put("Month", event.getPriorTime().getMonth());
		mail.getModel().put("Year", event.getPriorTime().getYear());
		mail.getModel().put("EmployeeId", String.valueOf(event.getPriorTime().getEmployeeId()));

		templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = templateConfiguration.getTemplate("email-verification.ftl");
		String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());

		mail.setContent(mailContent);
		send(mail);

	}

	@Override
	public void sendAccountChangeEmail(OnPriorTimeAcceptOrRejectEvent event, String action, String actionStatus,
			String to) throws IOException, TemplateException, MessagingException {
		Mail mail = new Mail();
		mail.setSubject("Timesheet Saved");
		mail.setTo(to);
		mail.setFrom(sender);
		mail.getModel().put("userName", to);
		mail.getModel().put("action", action);
		mail.getModel().put("actionStatus", actionStatus);
		mail.getModel().put("CheckIn", event.getPriortime().get().getCheckIn());
		mail.getModel().put("CheckOut", event.getPriortime().get().getCheckOut());
		mail.getModel().put("Date", event.getPriortime().get().getDate());
		mail.getModel().put("Email", event.getPriortime().get().getEmail());
		mail.getModel().put("Month", event.getPriortime().get().getMonth());
		mail.getModel().put("Year", event.getPriortime().get().getYear());
		mail.getModel().put("WorkingHour", event.getPriortime().get().getWorkingHour());

		templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = templateConfiguration.getTemplate("account-activity-change.ftl");
		String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
		mail.setContent(mailContent);
		send(mail);

	}

	@Override
	public void send(Mail mail) throws MessagingException, UnsupportedEncodingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());

		helper.setTo(mail.getTo());
		helper.setText(mail.getContent(), true);
		helper.setSubject(mail.getSubject());
		helper.setFrom(mail.getFrom());
		mailSender.send(message);
	}
	//*** END:- For Prior Time ***

	//*** START:- To create payslip and then send mail ***
	@Override
	public void sendEmail(ByteArrayOutputStream baos, String name, String gmail, String monthYear) {
		String massage = Util.msg.replace("[Name]", name).replace("[Your Name]", "AlphaDot Technologies")
				.replace("[Month, Year]", monthYear);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper;

		try {

			DataSource source = new ByteArrayDataSource(baos.toByteArray(), "application/octet-stream");
			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setFrom(sender);
			mimeMessageHelper.setTo(gmail);
			mimeMessageHelper.setText(massage);
			mimeMessageHelper.setSubject("Salary Slip" + "-" + monthYear);
			mimeMessageHelper.addAttachment(name + ".pdf", source);

			mailSender.send(mimeMessage);

			log.info("Mail send Successfully");
		} catch (MessagingException e) {
			log.info("Error");

		}
	}
	//*** END:- To create payslip and then send mail ***

	//*** START:- To send mail for Leave Request ***
	@Override
	public String sendEmail(OnLeaveRequestSaveEvent event, String Url, String Url1, LeaveRequestModel lr)
			throws IOException, TemplateException, MessagingException {
		Mail mail =  new Mail();
		mail.setSubject("Leave Request");
		//*** From whom the mail should come ***
		mail.setFrom(sender);

		//*** To whom we should send the mail ***
		Integer empID = lr.getEmpid();
		Optional<User> user = userRepo.findById(empID);
		String userEmail = user.get().getEmail();
		mail.setTo(userEmail);

		mail.getModel().put("leaveApprovalLink", Url);
		mail.getModel().put("leaveRejectionLink", Url1);
		mail.getModel().put("LeaveId", event.getLeaveRequestModel().getLeaveid().toString() );
		mail.getModel().put("EmpId", event.getLeaveRequestModel().getEmpid().toString());
		mail.getModel().put("Name", event.getLeaveRequestModel().getName());
		mail.getModel().put("LeaveBalance", event.getLeaveRequestModel().getLeaveBalance().toString());
		mail.getModel().put("LeaveType", event.getLeaveRequestModel().getLeaveType());
		mail.getModel().put("Reason", event.getLeaveRequestModel().getLeaveReason());
		mail.getModel().put("LeaveDates", event.getLeaveRequestModel().getLeavedate().toString());
		mail.getModel().put("Status", event.getLeaveRequestModel().getStatus());

		templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = templateConfiguration.getTemplate("leave_status_change.ftl");
		String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
		mail.setContent(mailContent);
		send(mail);

		return "Mail Sent Successfully";
	}

	@Override
	public void sendEmail(OnLeaveRequestSaveEvent event) {
		LeaveRequestModel leaveRequestModel = event.getLeaveRequestModel();
		String emailApprovalUrl = event.getRedirectUrl().toUriString();
		String emailRejectionUrl = event.getRedirectUrl1().toUriString();

		try {
			sendEmail(event, emailApprovalUrl, emailRejectionUrl, leaveRequestModel);
		} catch (IOException | TemplateException | MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public void sendEmployeeExpenseVerification(OnEmployeeExpenseDetailsSavedEvent event, String emailVerificationUrl1, String to) throws IOException, TemplateException, MessagingException {
		EmployeeExpenseDTO employeeExpenseDTO =event.getEmployeeExpenseDTO();
		Mail mail = new Mail();
		mail.setSubject("Employee Expense Request...");
		mail.setTo(to);
		mail.setFrom(mailFrom);
		mail.getModel().put("expenseId",employeeExpenseDTO.getExpenseId()+"");
		mail.getModel().put("EmployeeName",employeeExpenseDTO.getEmpName());
		mail.getModel().put("approveEmployeeExpenseLink1", emailVerificationUrl1);
		mail.getModel().put("Email",to);
		mail.getModel().put("expenseAmount", employeeExpenseDTO.getExpenseAmount());
		mail.getModel().put("comments", employeeExpenseDTO.getEmployeeComments());
		mail.getModel().put("expenseDescription", employeeExpenseDTO.getExpenseDescription());
		mail.getModel().put("expenseCategory", employeeExpenseDTO.getExpenseCategory());
		mail.getModel().put("paymentDate", employeeExpenseDTO.getPaymentDate());
		mail.getModel().put("paymentMode", employeeExpenseDTO.getPaymentMode());
		mail.getModel().put("submitDate", employeeExpenseDTO.getSubmitDate());
		if( employeeExpenseDTO.getInvoices() != null) {
			mail.setAttachments(employeeExpenseDTO.getInvoices());
		}
		templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = templateConfiguration.getTemplate("employee_expense_request.ftl");
		String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
		mail.setContent(mailContent);
		send(mail);
	}

	public void sendEmployeeExpenseApprovalEmail(OnEmployeeExpenseAcceptOrRejectEvent event, String action, String actionStatus, String to)
			throws IOException, TemplateException, MessagingException {
		EmployeeExpenseDTO employeeExpenseDTO = event.getEmployeeExpenseDTO();
		Mail mail = new Mail();
		mail.setSubject("Expense Approval");
		mail.setTo(to);
		mail.setFrom(mailFrom);
		mail.getModel().put("employeeName", employeeExpenseDTO.getEmpName());
		mail.getModel().put("Email", to);
		mail.getModel().put("action", action);
		mail.getModel().put("actionStatus", actionStatus);
		mail.getModel().put("expenseAmount", employeeExpenseDTO.getExpenseAmount());
		mail.getModel().put("Comments", employeeExpenseDTO.getEmployeeComments());
		mail.getModel().put("expenseDescription", employeeExpenseDTO.getExpenseDescription());
		mail.getModel().put("expenseCategory", employeeExpenseDTO.getExpenseCategory());
		mail.getModel().put("paymentDate", employeeExpenseDTO.getPaymentDate());
		mail.getModel().put("paymentMode", employeeExpenseDTO.getPaymentMode());
		mail.getModel().put("submitDate", employeeExpenseDTO.getSubmitDate());
		if(employeeExpenseDTO.getInvoices() != null) {
			mail.setAttachments(employeeExpenseDTO.getInvoices());
		}
		templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = templateConfiguration.getTemplate("employee_expense_approve.ftl");
		String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
		mail.setContent(mailContent);
		send(mail);
	}

	@Override
	public void sendAccountChangeEmailApproved(OnEmployeeExpenseAcceptOrRejectEvent event)
			throws IOException, TemplateException, MessagingException {
		// TODO Auto-generated method stub
		
	}
	
	
	
	

}
