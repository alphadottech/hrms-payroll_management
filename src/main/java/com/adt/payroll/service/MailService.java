/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adt.payroll.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.adt.payroll.event.OnPriorTimeAcceptOrRejectEvent;
import com.adt.payroll.event.OnPriorTimeDetailsSavedEvent;
import com.adt.payroll.model.Mail;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Service
public class MailService {

	private final JavaMailSender mailSender;

	private final Configuration templateConfiguration;

	@Value("${app.velocity.templates.location}")
	private String basePackagePath;

	@Value("${spring.mail.username}")
	private String mailFrom;


	public MailService(JavaMailSender mailSender, Configuration templateConfiguration) {
		this.mailSender = mailSender;
		this.templateConfiguration = templateConfiguration;
	}

	public void sendEmailVerification(OnPriorTimeDetailsSavedEvent event, String emailVerificationUrl1,
			String emailVerificationUrl2, String to) throws IOException, TemplateException, MessagingException {
		Mail mail = new Mail();
		mail.setSubject("Email Verification [Team CEP]");
		mail.setTo(to);
		mail.setFrom(mailFrom);
		
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

	
	 /**
     * Send an email to the user indicating an account change event with the correct
     * status
     */
    public void sendAccountChangeEmail(OnPriorTimeAcceptOrRejectEvent event,String action, String actionStatus, String to)
            throws IOException, TemplateException, MessagingException {
        Mail mail = new Mail();
        mail.setSubject("Timesheet Saved");
        mail.setTo(to);
        mail.setFrom(mailFrom);
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
    
    
	/**
	 * Sends a simple mail as a MIME Multipart message
	 * 
	 * @throws UnsupportedEncodingException
	 */
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

}
