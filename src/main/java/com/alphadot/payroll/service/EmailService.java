package com.alphadot.payroll.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailService {
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	public String sendEmail(String email) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom("alphadottest@gmail.com");
		msg.setTo("spyavenger55@gmail.com");
		msg.setCc(email);
		msg.setSubject("Test Subject");
		msg.setText("Test Body");
		
		javaMailSender.send(msg);
		
		return "Mail Sent Successfully";
		
	}
	
	 

}
