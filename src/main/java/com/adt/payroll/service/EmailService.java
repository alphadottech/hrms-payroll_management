package com.adt.payroll.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.model.Mail;
import com.adt.payroll.model.OnLeaveRequestSaveEvent;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

	//	@Autowired
	//	private EmployeeRepo employeeRepo;

	@Autowired
	private JavaMailSender javaMailSender;

	private final Configuration templateConfiguration;


	@Value("${app.velocity.templates.location}")
	private String basePackagePath;

	@Autowired
	public EmailService(JavaMailSender javaMailSender, Configuration templateConfiguration ) {
		this.javaMailSender = javaMailSender;
		this.templateConfiguration = templateConfiguration;
	}

	public String sendEmail(OnLeaveRequestSaveEvent event, String Url, String Url1, LeaveRequestModel lr)
			throws IOException, TemplateException, MessagingException{

		Mail mail =  new Mail();
		mail.setSubject("Leave Request");
		mail.setFrom("mukeshchandalwar.adt@gmail.com");
		mail.setTo("dhananjaybobde.adt@gmail.com");
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

	public String send(Mail mail) throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());

		helper.setTo(mail.getTo());
		helper.setText(mail.getContent(), true);
		helper.setSubject(mail.getSubject());
		helper.setFrom(mail.getFrom());
		javaMailSender.send(message);

		return "Mail Sent Successfully";
	}




}
