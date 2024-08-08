package com.adt.payroll.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

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

import com.adt.payroll.config.Auth;
import com.adt.payroll.event.OnLeaveAcceptOrRejectEvent;
import com.adt.payroll.model.LeaveBalance;
import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.model.Mail;
import com.adt.payroll.model.OnLeaveRequestSaveEvent;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.LeaveBalanceRepository;
import com.adt.payroll.repository.LeaveRepository;
import com.adt.payroll.repository.LeaveRequestRepo;
import com.adt.payroll.repository.UserRepo;

import freemarker.template.TemplateException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
	

    @Value("${-Dmy.port}")
	private String serverPort;

	@Value("${-Dmy.property}")
	private String ipaddress;
	
	@Value("${-UI.scheme}")
	private String scheme;

	@Value("${-UI.context}")
	private String context;
	
	@Autowired
	private Auth auth;
       
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	private LeaveBalanceRepository leaveBalanceRepo;
	
	@Autowired
	private TableDataExtractor dataExtractor;

	public LeaveRequestServiceImpl() {

	}

	public LeaveRequestServiceImpl(LeaveRequestRepo leaveReqRepo) {
		this.leaveRequestRepo = leaveReqRepo;
	}

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
		String sql="select leave_balance from payroll_schema.leave_balance where emp_id="+lr.getEmpid();
		 List<Map<String, Object>> tableData = dataExtractor.extractDataFromTable(sql);
		 Map<String, Object> firstMap = tableData.get(0);
		 lr.setLeaveBalance(Integer.valueOf(String.valueOf(firstMap.get("leave_balance"))));
			
		if (counter == 0) {
			lr.setStatus("Pending");
			long millisecondsInFiveDays = TimeUnit.DAYS.toMillis(5);
			long currentTime=System.currentTimeMillis();
			leaveRequestRepo.save(lr);
			lr.setExpiryTime(currentTime+millisecondsInFiveDays);
			int id = lr.getEmpid();
			int leaveId = lr.getLeaveid();
			 UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.newInstance()
						.scheme(scheme)
						.host(ipaddress)
						.port(serverPort)
						.path(context+"/payroll/leave/leave/Accepted/"+ id + "/" + leaveId + "/" + lr.getLeavedate().size()+"/"+lr.getLeaveType()+"/"+lr.getLeaveReason());
			            
 
			 UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.newInstance()
						.scheme(scheme)
						.host(ipaddress)
						.port(serverPort)
						.path(context+"/payroll/leave/leave/Rejected/"+ id + "/" + leaveId+"/"+lr.getLeaveType()+"/"+lr.getLeaveReason());
			           
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
	public Page<LeaveRequestModel> getLeaveRequestDetailsByEmpId(int page, int size,Integer empid) {
		LOGGER.info("Payroll service: LeaveRequestServiceImpl:  getLeaveRequestDetailsByEmpId Info level log msg");
		long currentTime1=System.currentTimeMillis();
		long millisecondsInFiveDays1 = TimeUnit.DAYS.toMillis(5);
		Pageable pageable = PageRequest.of(page, size);
		Page<LeaveRequestModel> leaveResponse = leaveRequestRepo.findByempid(empid,pageable);
		for(LeaveRequestModel leave : leaveResponse) {
			if(leave.getStatus().equalsIgnoreCase("Pending")) {
				long millisecondsInFiveDays = TimeUnit.DAYS.toMillis(5);
				long currentTime=System.currentTimeMillis();
				if(leave.getExpiryTime()<currentTime) {
					leave.setStatus("Resend");
					leaveRequestRepo.save(leave);
				}
			}
			
		}
		if (!leaveResponse.isEmpty()) {
			return leaveResponse;
		} else {
			return null;
		}

	}

	@Override
	public String AcceptLeaveRequest(Integer empid, Integer leaveId, Integer leaveDate ,String leaveType,String leaveReason)
			throws TemplateException, MessagingException, IOException {
		Optional<LeaveRequestModel> leaveRequest = Optional.of(new LeaveRequestModel());
		LeaveRequestModel leaveR = leaveRequestRepo.search(empid, leaveId);
		Optional<User> user = Optional.ofNullable(userRepo.findById(empid)
				.orElseThrow(() -> new EntityNotFoundException("employee not found :" + empid)));
		String email = user.get().getEmail();
		if (leaveR != null && leaveR.getStatus().equalsIgnoreCase("Pending")) {
			String message = "Accepted";
			LeaveBalance leaveBalance = leaveBalanceRepo.findByEmpId(empid);
			if (leaveBalance.getLeaveBalance() >= leaveDate) {
				leaveBalance.setLeaveBalance(leaveBalance.getLeaveBalance() - leaveDate);
				leaveBalance.setPaidLeave(leaveBalance.getPaidLeave() + leaveDate);
			} else {
				int leaveNo = leaveDate - leaveBalance.getLeaveBalance();
				leaveBalance.setPaidLeave(leaveBalance.getPaidLeave() + leaveBalance.getLeaveBalance());
				leaveBalance.setUnpaidLeave(leaveNo);
				leaveBalance.setLeaveBalance(0);
			}
			List<String> leaveDatelist = new ArrayList<>();
			String sql = "select leavedate from payroll_schema.leave_dates where leave_id=" + leaveId;
			List<Map<String, Object>> leaveData = dataExtractor.extractDataFromTable(sql);
			for (Map<String, Object> leaveMap : leaveData) {
				leaveDatelist.add((String.valueOf(leaveMap.get("leavedate"))));
			}
			leaveRequest.get().setName(leaveBalance.getName());
			leaveRequest.get().setLeaveType(leaveType);
			leaveRequest.get().setLeaveReason(leaveReason);
			leaveRequest.get().setLeavedate(leaveDatelist);
			leaveRequest.get().setStatus(message);
			leaveRequest.get().setLeaveBalance(leaveBalance.getLeaveBalance());
			leaveRequest.get().setEmail(email);
			leaveRequest.get().setMessage("Your leave request has been approved. Find blow leave request approved details");
			leaveR.getLeaveBalance();
			OnLeaveAcceptOrRejectEvent onLeaveAcceptOrRejectEvent = new OnLeaveAcceptOrRejectEvent(leaveRequest);
			applicationEventPublisher.publishEvent(onLeaveAcceptOrRejectEvent);
			leaveR.setStatus("Accepted");
			leaveBalanceRepo.save(leaveBalance);
			leaveR.setUpdatedBy(auth.getEmail());	
			leaveRequestRepo.save(leaveR);
			return leaveR.getLeaveid() + " leave Request Accepted";
		} else {
			return empid + "leave request status already updated";
		}

	}

	@Override
	public String RejectLeaveRequest(Integer empid, Integer leaveId,String leaveType,String leaveReason)
			throws TemplateException, MessagingException, IOException {
		Optional<LeaveRequestModel> leaveRequest = Optional.of(new LeaveRequestModel());
		LeaveRequestModel leaveR = leaveRequestRepo.search(empid, leaveId);
		Optional<User> user = Optional.ofNullable(userRepo.findById(empid)
				.orElseThrow(() -> new EntityNotFoundException("employee not found :" + empid)));
		String email = user.get().getEmail();
		LeaveBalance leaveBalance = leaveBalanceRepo.findByEmpId(empid);
		if (leaveR != null && leaveR.getStatus().equalsIgnoreCase("Pending")) {
			List<String> leaveDatelist = new ArrayList<>();
			String sql1 = "select leavedate from payroll_schema.leave_dates where leave_id=" + leaveId;
			List<Map<String, Object>> leaveData = dataExtractor.extractDataFromTable(sql1);
			for (Map<String, Object> leaveMap : leaveData) {
				leaveDatelist.add((String.valueOf(leaveMap.get("leavedate"))));
			}
			String message = "Rejected";
			String sql = "delete from payroll_schema.leave_dates where leave_id=" + leaveId;
			dataExtractor.insertDataFromTable(sql);
			leaveRequest.get().setName(leaveBalance.getName());
			leaveRequest.get().setLeaveType(leaveType);
			leaveRequest.get().setLeaveReason(leaveReason);
			leaveRequest.get().setLeavedate(leaveDatelist);
			leaveRequest.get().setStatus(message);
			leaveRequest.get().setLeaveBalance(leaveBalance.getLeaveBalance());
			leaveRequest.get().setEmail(email);
			leaveRequest.get().setMessage("Your leave request has been rejected. Find blow leave request rejected details");
			leaveR.setStatus("Rejected");
			leaveR.setUpdatedBy(auth.getEmail());
			leaveRequestRepo.save(leaveR);
			OnLeaveAcceptOrRejectEvent onLeaveAcceptOrRejectEvent = new OnLeaveAcceptOrRejectEvent(leaveRequest);
			applicationEventPublisher.publishEvent(onLeaveAcceptOrRejectEvent);
			return leaveR.getLeaveid() + " leave Request Rejected";
		} else {
			return empid + " leave request status already updated";
		}
	}

	public void sendEmail(String to, String messages) throws IOException, TemplateException, MessagingException {
		Mail mail = new Mail();
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
	
	@Override
	public List<LeaveRequestModel> getAllEmployeeLeaveDetails() {
		return leaveRequestRepo.findAll();
	}
	
     public  String reSendLeaveRequest(int leaveId) {
    	 Optional<LeaveRequestModel>  leaveRequest= leaveRequestRepo.findById(leaveId);
    	if(leaveRequest.isPresent()) {
    		int id=leaveRequest.get().getEmpid();
    		Optional<LeaveModel>leaveBalance= leaveRepository.findById(id);
    		LeaveRequestModel leaveReq=	leaveRequest.get();
    		leaveReq.setLeaveBalance(leaveBalance.get().getLeaveBalance());
    		leaveReq.setStatus("Pending");
    		leaveReq.setName(leaveBalance.get().getName());
    		long millisecondsInFiveDays = TimeUnit.DAYS.toMillis(5);
    		long currentTime=System.currentTimeMillis();
    		leaveReq.setExpiryTime(currentTime+millisecondsInFiveDays);
    		 UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.newInstance()
						.scheme(scheme)
						.host(ipaddress)
						.port(serverPort)
						.path(context+"/payroll/leave/leave/Accepted/"+ id + "/" + leaveId + "/" + leaveRequest.get().getLeavedate().size()+"/"+leaveRequest.get().getLeaveType()+"/"+leaveRequest.get().getLeaveReason());			            

			 UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.newInstance()
						.scheme(scheme)
						.host(ipaddress)
						.port(serverPort)
						.path(context+"/payroll/leave/leave/Rejected/"+ id + "/" + leaveId+"/"+leaveRequest.get().getLeaveType()+"/"+leaveRequest.get().getLeaveReason());
			           
				OnLeaveRequestSaveEvent onLeaveRequestSaveEvent = new OnLeaveRequestSaveEvent(urlBuilder, urlBuilder1, leaveReq);
			applicationEventPublisher.publishEvent(onLeaveRequestSaveEvent);
			leaveRequestRepo.save(leaveReq);
			return "reSend email successfully";
    	}
    	return "this records  not persent "; 
    	 
     }
		@Override
		public LeaveRequestModel getLeaveRequestDetailsByEmpIdAndLeaveId(int empId, int leaveId) {
			return (LeaveRequestModel) leaveRequestRepo.findByEmpidAndLeaveid(empId, leaveId)
					.orElseThrow(() -> new IllegalArgumentException("Leave request not found for empId: " + empId + " and leaveId: " + leaveId));
		}
	}



