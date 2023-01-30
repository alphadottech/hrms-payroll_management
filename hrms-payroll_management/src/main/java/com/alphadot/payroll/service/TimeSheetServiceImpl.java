package com.alphadot.payroll.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.repository.TimeSheetRepo;

@Service
public class TimeSheetServiceImpl implements TimeSheetService {

	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Override
	public String updateCheckIn(int id) {

		TimeSheetModel timeSheetModel = new TimeSheetModel();
		/*
		 * Employee employee =employeeRepo.findByEmpId(id); String name =
		 * employee.getFName();
		 */

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime localDateTime = LocalDateTime.now();
		String time = String.valueOf(dateTimeFormatter.format(localDateTime));

		LocalDate localDate = LocalDate.now();
		String date = String.valueOf(localDate);

		timeSheetModel.setDate(date);
		timeSheetModel.setEmployeeId(id);

		timeSheetModel.setCheckIn(time);
		timeSheetRepo.save(timeSheetModel);

		return "check In successfully AT :" + time;
	}

	@Override
	public String updateCheckOut(int id) {
		TimeSheetModel timeSheetStatus = new TimeSheetModel();
//        Employee employee =employeeRepo.findByEmpId(id);
//        String name = employee.getFName();

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

			return " checkout successfully AT :" + time;
		} catch (Exception e) {
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
				return false;

			return null;
		} else
			return true;

	}

}
