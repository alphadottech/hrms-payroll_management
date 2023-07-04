package com.adt.payroll.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import com.adt.payroll.model.*;
import com.adt.payroll.repository.LeaveRepository;
import com.adt.payroll.repository.UserRepo;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.adt.payroll.repository.LeaveRequestRepo;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityNotFoundException;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private LeaveRequestRepo leaveRequestRepo;

	@Autowired
	private LeaveRepository leaveRepository;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private UserRepo userRepo;
	@Value("${spring.mail.username}")
	private String sender;

	private final ApplicationEventPublisher applicationEventPublisher;

	public LeaveRequestServiceImpl(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public String saveLeaveRequest(LeaveRequestModel lr) {

		LOGGER.info("Payroll service: LeaveRequestServiceImpl:  saveLeaveRequest Info level log msg");

		List<LeaveRequestModel> opt = leaveRequestRepo.findByempid(lr.getEmpid());
		int counter = 0;
		for (LeaveRequestModel lrm : opt) {
			List<String> dbld = lrm.getLeavedate();
			List<String> uild = lr.getLeavedate();
			for (String tempLd : uild) {
				if (dbld.contains(tempLd)) {
					counter++;
				}
			}
		}
		if (counter == 0) {
			//List<String> li = lr.getLeavedate();
			//	lr.setLeavedate(li);
			lr.setStatus("Pending");

			leaveRequestRepo.save(lr);
			int id = lr.getEmpid();
			int leaveId = lr.getLeaveid();
			UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/leave/leave/Accepted/" + id + "/" + leaveId+ "/"+lr.getLeavedate().size());
			UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/leave/leave/Rejected/" + id + "/" + leaveId);
			
			System.out.println(urlBuilder.toUriString());
			System.out.println(urlBuilder1.toUriString());

			//				UriComponentsBuilder urlBuilder2 = ServletUriComponentsBuilder.fromHttpUrl("http://localhost:9095/payroll/leave/leave/Rejected/"+id+"/"+leaveId);

			OnLeaveRequestSaveEvent onLeaveRequestSaveEvent = new OnLeaveRequestSaveEvent(urlBuilder, urlBuilder1, lr);
			applicationEventPublisher.publishEvent(onLeaveRequestSaveEvent);

		} else {
			return "you have selected wrong date OR already requested for selected date";
		}


		return lr.getLeaveid() + " Leave Request is saved & mail Sent Successfully";
	}

	@Override
	public List<LeaveRequestModel> getLeaveDetails() {
		LOGGER.info("Payroll service: LeaveRequestServiceImpl:  getLeaveDetails Info level log msg");
		List<LeaveRequestModel> leavelist = leaveRequestRepo.findAll();
		return leavelist;
	}

	@Override
	public List<LeaveRequestModel> getLeaveRequestDetailsByEmpId(Integer empid) {
		LOGGER.info("Payroll service: LeaveRequestServiceImpl:  getLeaveRequestDetailsByEmpId Info level log msg");
		List<LeaveRequestModel> opt = leaveRequestRepo.findByempid(empid);
		if (!opt.isEmpty()) {
			return opt;
		} else {
			return null;
		}

	}

	@Override
	public String AcceptLeaveRequest(Integer empid, Integer leaveId,Integer leaveDate) throws TemplateException, MessagingException, IOException {

		LeaveRequestModel opt = leaveRequestRepo.search(empid, leaveId);

		Optional<User> user = Optional.ofNullable(userRepo.findById(empid)
				.orElseThrow(() -> new EntityNotFoundException("employee not found :" + empid)));
		String email = user.get().getEmail();

		if (opt != null && opt.getStatus().equalsIgnoreCase("Pending")) {
			String message = "Accepted";

			LeaveModel leaveModel = leaveRepository.findByEmpId(empid);


			if(leaveModel.getLeaveBalance()>=leaveDate) {


				sendEmail(email,message);

				opt.setStatus("Accepted");
				leaveRequestRepo.save(opt);
				leaveModel.setLeaveBalance(leaveModel.getLeaveBalance() - leaveDate);
			}
			else{

				return "You dont have sufficient Leave Balance";
			}
			leaveRepository.save(leaveModel);
			return opt.getLeaveid() + " leave Request Accepted";
		} else {
			return empid + "leave request status already updated";
		}

	}

	@Override
	public String RejectLeaveRequest(Integer empid, Integer leaveId) throws TemplateException, MessagingException, IOException {
		LeaveRequestModel opt = leaveRequestRepo.search(empid, leaveId);
		Optional<User> user = Optional.ofNullable(userRepo.findById(empid)
				.orElseThrow(() -> new EntityNotFoundException("employee not found :" + empid)));
		String email = user.get().getEmail();

		if (opt != null && opt.getStatus().equalsIgnoreCase("Pending")) {
			String message = "Rejected";

			sendEmail(email,message);
			opt.setStatus("Rejected");
			leaveRequestRepo.save(opt);
			return opt.getLeaveid() + " leave Request Rejected";
		} else {
			return empid + " leave request status already updated";
		}
	}

	public void sendEmail(String to, String messages) throws IOException, TemplateException, MessagingException {
		Mail mail =  new Mail();
		mail.setSubject("Leave Request");
		mail.setFrom(sender);
		mail.setTo(to);
		String emailContent;
		if (messages.equals("Accepted")) {
			emailContent = "<html><body><h1>Leave Request Accepted</h1><p>Your leave request has been accepted.</p></body></html>";
		} else {
			emailContent = "<html><body><h1>Leave Request Rejected</h1><p>Sorry, your leave request has been rejected.</p></body></html>";
		}
		mail.setContent(emailContent);
		mail.getModel().put("LeaveStatus", messages);


		mail.setContent(emailContent);



		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());

		helper.setTo(mail.getTo());
		helper.setText(mail.getContent(), true);
		helper.setSubject(mail.getSubject());
		helper.setFrom(mail.getFrom());
		javaMailSender.send(message);

		LOGGER.info("Mail send Successfully");

	}

}