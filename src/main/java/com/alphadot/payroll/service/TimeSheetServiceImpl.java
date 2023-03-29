package com.alphadot.payroll.service;

import java.text.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.List;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.Priortime;
import com.alphadot.payroll.model.TimeSheetModel;

import com.alphadot.payroll.msg.ResponseModel;

import com.alphadot.payroll.model.payload.PriorTimeManagementRequest;

import com.alphadot.payroll.repository.PriorTimeRepository;

import com.alphadot.payroll.repository.TimeSheetRepo;

@Service
public class TimeSheetServiceImpl implements TimeSheetService {

	private static final Logger log = LogManager.getLogger(TimeSheetService.class);

	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Autowired
	PriorTimeRepository priorTimeRepository;

	@Override
	public ResponseModel updateCheckIn(int id) {
		ResponseModel responseModel = new ResponseModel();

		log.warn("Do not checkIn again once its done");
		TimeSheetModel timeSheetModel = new TimeSheetModel();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime localDateTime = LocalDateTime.now();
		String time = String.valueOf(dateTimeFormatter.format(localDateTime));

		LocalDate localDate = LocalDate.now();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String date = String.valueOf(localDate.format(dateFormat));

		LocalDate currentdate = LocalDate.now();
		Month currentMonth = currentdate.getMonth();

		timeSheetModel.setDate(date);
		timeSheetModel.setEmployeeId(id);
		timeSheetModel.setMonth(String.valueOf(currentMonth));
		timeSheetModel.setCheckIn(time);
		timeSheetModel.setYear(String.valueOf(currentdate.getYear()));
		timeSheetModel.setDate(date);
		timeSheetModel.setEmployeeId(id);
		timeSheetModel.setMonth(String.valueOf(currentMonth));
		timeSheetModel.setCheckIn(time);
		timeSheetModel.setYear(String.valueOf(currentdate.getYear()));
		timeSheetRepo.save(timeSheetModel);

		log.info("successfully done checkIn and returning to controller");
		responseModel.setMsg("check In successfully AT :" + time);
		return responseModel;
	}

	@Override
	public ResponseModel updateCheckOut(int id) {

		ResponseModel responseModel = new ResponseModel();

		log.warn("Do not checkOut again once its done");

		TimeSheetModel timeSheetStatus = new TimeSheetModel();
		LocalDate currentdate = LocalDate.now();
		Month currentMonth = currentdate.getMonth();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime localDateTime = LocalDateTime.now();
		String time = String.valueOf(dateTimeFormatter.format(localDateTime));

		LocalDate localDate = LocalDate.now();

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String date = String.valueOf(localDate.format(dateFormat));

		TimeSheetModel timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(id, date);

		timeSheetStatus.setDate(date);
		timeSheetStatus.setEmployeeId(id);

		try {
			timeSheetModel.setCheckOut(time);

			timeSheetModel.setMonth(String.valueOf(currentMonth));
			timeSheetModel.setYear(String.valueOf(currentdate.getYear()));
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

			responseModel.setMsg(" checkout successfully AT :" + time);
			return responseModel;
		} catch (ParseException e) {
			log.error("Please do a valid checkOut");
			responseModel.setMsg("NOt a valid checkout");

			return responseModel;
		}

	}

	@Override
	public ResponseModel checkStatus(int empId) {
		ResponseModel responseModel = new ResponseModel();

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDateTime localDateTime = LocalDateTime.now();
		String date = dateTimeFormatter.format(localDateTime);
		TimeSheetModel timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(empId, date);

		if (timeSheetModel != null) {
			if (timeSheetModel.getCheckOut() == null) {
				log.info("You are checked In today and Not checkOut so you are elligible to checkout");
				responseModel.setTimeSheetStatus(false);
				return responseModel;
			}
			log.info("you are already checkout for the day");
			responseModel.setTimeSheetStatus(null);
			return responseModel;
		} else {
			log.info("You are Not checked In today So you are elligible to Do CheckIn");
			responseModel.setTimeSheetStatus(true);
			return responseModel;
		}
	}

	// priorTimeaAjustment
	@Override
	public ResponseModel checkPriorStatus(int empId) {
		ResponseModel responseModel = new ResponseModel();

		TimeSheetModel timeSheetModel;

		List<String> list = new ArrayList<>();

		SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
		Calendar cal = Calendar.getInstance();

		int temp = 15;

		while (temp > 0) {
			String date = f.format(cal.getTime());
			cal.add(Calendar.DATE, -1);
			timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(empId, date);

			if (timeSheetModel == null)
				list.add(date);
			else if (timeSheetModel.getCheckOut() == null)
				list.add(timeSheetModel.toString());

			temp--;
		}
		responseModel.setPriorResult(list);
		return responseModel;
	}

	public Optional<Priortime> savePriorTime(PriorTimeManagementRequest priorTimeManagementRequest)
			throws ParseException {

		Priortime priortimeuser = new Priortime();
		if (priorTimeManagementRequest.getCheckIn() != null && !priorTimeManagementRequest.getCheckIn().equals("")) {
			priortimeuser.setCheckIn(priorTimeManagementRequest.getCheckIn());
		} else {
			TimeSheetModel timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(
					priorTimeManagementRequest.getEmployeeId(), priorTimeManagementRequest.getDate());
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

	public TimeSheetModel saveConfirmedDetails(Optional<Priortime> priortime) throws ParseException {
		Integer employeeId = priortime.get().getEmployeeId();
		String date = priortime.get().getDate();

		if ((priortime.get().getCheckIn()) == null || (priortime.get().getCheckIn()) == null) {

			TimeSheetModel timesheet = timeSheetRepo.findByEmployeeIdAndDate(employeeId, date);

			timesheet.setCheckIn(priortime.get().getCheckIn());
			timesheet.setCheckOut(priortime.get().getCheckOut());
			timesheet.setStatus("PRESENT");
			timesheet.setWorkingHour(priortime.get().getWorkingHour());

			return timeSheetRepo.save(timesheet);
		} else {
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

	public List<TimeSheetModel> empAttendence(int empId, LocalDate fromDate, LocalDate toDate) {

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		String startDate = String.valueOf(dateTimeFormatter.format(fromDate));
		String endDate = String.valueOf(dateTimeFormatter.format(toDate));


		List<TimeSheetModel> list = timeSheetRepo.findAllByEmployeeId(empId, startDate, endDate);
		if (list.isEmpty())
			throw new NullPointerException("No attendence data available with given ID");

		return list;
	}

	@Override
	public List<TimeSheetModel> allEmpAttendence(LocalDate fromDate, LocalDate toDate) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		String startDate = String.valueOf(dateTimeFormatter.format(fromDate));
		String endDate = String.valueOf(dateTimeFormatter.format(toDate));


		
		List<TimeSheetModel> list = timeSheetRepo.findAllByEmployeeId(startDate, endDate);
		return list;
	}
}
