package com.alphadot.payroll.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.repository.TimeSheetRepo;

@Service
public class TimeSheetServiceImpl implements TimeSheetService {

	private static final Logger log=LogManager.getLogger(TimeSheetService.class);
	
	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Override
	public String updateCheckIn(int id) {
    log.warn("Do not checkIn again once its done");
		TimeSheetModel timeSheetModel = new TimeSheetModel();

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime localDateTime = LocalDateTime.now();
		String time = String.valueOf(dateTimeFormatter.format(localDateTime));

		LocalDate localDate = LocalDate.now();
		String date = String.valueOf(localDate);

		timeSheetModel.setDate(date);
		timeSheetModel.setEmployeeId(id);

		timeSheetModel.setCheckIn(time);
		timeSheetRepo.save(timeSheetModel);
    log.info("successfully done checkIn and returning to controller");
		return "check In successfully AT :" + time;
	}

	@Override
	public String updateCheckOut(int id) {
		
		
	    log.warn("Do not checkOut again once its done");

		TimeSheetModel timeSheetStatus = new TimeSheetModel();

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime localDateTime = LocalDateTime.now();
		String time = String.valueOf(dateTimeFormatter.format(localDateTime));

		LocalDate localDate = LocalDate.now();
		String date = String.valueOf(localDate);
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
				return false;
			}	
			log.info("you are already checkout for the day");	
			return null;
		} else {
         log.info("You are Not checked In today So you are elligible to Do CheckIn");			
			return true;
		}

	}

}
