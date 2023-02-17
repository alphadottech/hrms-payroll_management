package com.alphadot.payroll.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.Priortime;
import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.model.payload.PriorTimeManagementRequest;
import com.alphadot.payroll.msg.Response;
import com.alphadot.payroll.repository.PriorTimeRepository;
import com.alphadot.payroll.repository.TimeSheetRepo;

@Service
public class TimeSheetServiceImpl implements TimeSheetService {

	private static final Logger log=LogManager.getLogger(TimeSheetService.class);
	
	@Autowired
	private TimeSheetRepo timeSheetRepo;
	
	@Autowired
	PriorTimeRepository priorTimeRepository;

	@Override
	public String updateCheckIn(int id) {
    log.warn("Do not checkIn again once its done");
		TimeSheetModel timeSheetModel = new TimeSheetModel();
		LocalDate currentdate = LocalDate.now();
		Month currentMonth = currentdate.getMonth();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime localDateTime = LocalDateTime.now();
		String time = String.valueOf(dateTimeFormatter.format(localDateTime));
        
	
		String date = String.valueOf(currentdate);
    
		timeSheetModel.setDate(date);
		timeSheetModel.setEmployeeId(id);
        timeSheetModel.setMonth(String.valueOf(currentMonth));
		timeSheetModel.setCheckIn(time);
		timeSheetModel.setYear(String.valueOf(currentdate.getYear()));
		timeSheetRepo.save(timeSheetModel);
    log.info("successfully done checkIn and returning to controller");
		return "check In successfully AT :" + time;
	}

	@Override
	public String updateCheckOut(int id) {
		
		
	    log.warn("Do not checkOut again once its done");

		TimeSheetModel timeSheetStatus = new TimeSheetModel();
		LocalDate currentdate = LocalDate.now();
		Month currentMonth = currentdate.getMonth();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime localDateTime = LocalDateTime.now();
		String time = String.valueOf(dateTimeFormatter.format(localDateTime));

		
		String date = String.valueOf(currentdate);
		TimeSheetModel timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(id, date);

		timeSheetStatus.setDate(date);
		timeSheetStatus.setEmployeeId(id);

		try {
			timeSheetModel.setCheckOut(time);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
			Date date1 = simpleDateFormat.parse(timeSheetModel.getCheckOut());
			Date date2 = simpleDateFormat.parse(timeSheetModel.getCheckIn());

			long differenceInMilliSeconds = Math.abs(date2.getTime() - date1.getTime());
			long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;
			long differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60;
			long differenceInSeconds = (differenceInMilliSeconds / 1000) % 60;

			timeSheetModel.setWorkingHour(differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds);
			timeSheetModel.setStatus("Present");
            timeSheetModel.setMonth(String.valueOf(currentMonth));
        	timeSheetModel.setYear(String.valueOf(currentdate.getYear()));
			timeSheetRepo.save(timeSheetModel);

			log.info("successfully done checkOut and returning to controller");

			return " checkout successfully AT :" + time;
		} catch (Exception e) {
			log.error("Please do a valid checkOut");
			return "An internal Error Occured!!";
		}

	}

	@Override
	public Boolean saveStatus(int empId) {

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime localDateTime = LocalDateTime.now();
		String date = dateTimeFormatter.format(localDateTime);

		TimeSheetModel timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(empId, date);

		if (timeSheetModel != null) {

			if (timeSheetModel.getCheckOut() == null)
			{
				log.info("You are checked In today and Not checkOut so you are elligible to checkout");	
				return Response.False;
			}	
			log.info("you are already checkout for the day");	
			return null;
		} else {
         log.info("You are Not checked In today So you are elligible to Do CheckIn");			
			return Response.True;
		}

	}
	
	public Optional<Priortime> savePriorTime(PriorTimeManagementRequest priorTimeManagementRequest)
			throws ParseException {

		Priortime priortimeuser = new Priortime();
		if (priorTimeManagementRequest.getCheckIn() != null && !priorTimeManagementRequest.getCheckIn().equals("") ) {
			priortimeuser.setCheckIn(priorTimeManagementRequest.getCheckIn());
		} else {
			TimeSheetModel timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(priorTimeManagementRequest.getEmployeeId(), priorTimeManagementRequest.getDate());
			timeSheetModel.getCheckIn();
			priortimeuser.setCheckIn(timeSheetModel.getCheckIn());

		}
		if (priorTimeManagementRequest.getCheckOut() != null && !priorTimeManagementRequest.getCheckOut().equals("")) {
			priortimeuser.setCheckOut(priorTimeManagementRequest.getCheckOut());
		} else {

			String checkout = timeSheetRepo.findCheckOutByEmployeeIdAndDate(priorTimeManagementRequest.getEmployeeId(),
					priorTimeManagementRequest.getDate());
			priortimeuser.setCheckOut(checkout);
		}

		priortimeuser.setDate(priorTimeManagementRequest.getDate());
		priortimeuser.setEmail(priorTimeManagementRequest.getEmail());
		priortimeuser.setEmployeeId(priorTimeManagementRequest.getEmployeeId());

		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");

		SimpleDateFormat monthFormatter = new SimpleDateFormat("MMMM");
		Date d = dateFormatter.parse(String.valueOf(priorTimeManagementRequest.getDate()));
		String month = monthFormatter.format(d);

		SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");
		Date y = dateFormatter.parse(String.valueOf(priorTimeManagementRequest.getDate()));
		String year = yearFormatter.format(y);
		
        DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");

		Date checkin = timeFormat.parse(priortimeuser.getCheckIn());
		Date checkout = timeFormat.parse(priortimeuser.getCheckOut());
		long differenceInMilliSeconds = Math.abs(checkin.getTime() - checkout.getTime());
		long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;
		long differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60;
		long differenceInSeconds = (differenceInMilliSeconds / 1000) % 60;

		priortimeuser.setWorkingHour(differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds);
		priortimeuser.setMonth(month.toUpperCase());
		priortimeuser.setYear(year.toUpperCase());

		Priortime priortime = priorTimeRepository.save(priortimeuser);

		return Optional.ofNullable(priortime);

	}


	
	@Override
	public TimeSheetModel saveConfirmedDetails(Optional<Priortime> priortime) throws ParseException {
		Integer employeeId = priortime.get().getEmployeeId();
		String date = priortime.get().getDate();
		

		if((priortime.get().getCheckIn())==null || (priortime.get().getCheckIn())==null){
		
			TimeSheetModel timesheet = timeSheetRepo.findByEmployeeIdAndDate(employeeId,date);

		timesheet.setCheckIn(priortime.get().getCheckIn());
		timesheet.setCheckOut(priortime.get().getCheckOut());
		timesheet.setStatus("PRESENT");
		timesheet.setWorkingHour(priortime.get().getWorkingHour());

		return timeSheetRepo.save(timesheet);
	}
		else {
			TimeSheetModel timesheet = new TimeSheetModel();
			timesheet.setCheckIn(priortime.get().getCheckIn());
			timesheet.setCheckOut(priortime.get().getCheckOut());
			timesheet.setDate(priortime.get().getDate());
			timesheet.setEmployeeId(priortime.get().getEmployeeId());
			timesheet.setMonth(priortime.get().getMonth());
			timesheet.setYear(priortime.get().getYear());
			timesheet.setWorkingHour(priortime.get().getWorkingHour());
			timesheet.setStatus("PRESENT");
			return timeSheetRepo.save(timesheet);

		}
		
	}

}
