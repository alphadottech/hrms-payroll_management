package com.alphadot.payroll.service;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.msg.ResponseModel;
import com.alphadot.payroll.repository.TimeSheetRepo;

@Service
public class TimeSheetServiceImpl implements TimeSheetService {

	private static final Logger log=LogManager.getLogger(TimeSheetService.class);
	
	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Override
	public ResponseModel updateCheckIn(int id) {
		ResponseModel responseModel=new ResponseModel();

        log.warn("Do not checkIn again once its done");
		TimeSheetModel timeSheetModel = new TimeSheetModel();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime localDateTime = LocalDateTime.now();
		String time = String.valueOf(dateTimeFormatter.format(localDateTime));

		LocalDate localDate = LocalDate.now();
		DateTimeFormatter dateFormat =DateTimeFormatter.ofPattern("dd-MM-yyyy");
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
		
		ResponseModel responseModel=new ResponseModel();

	    log.warn("Do not checkOut again once its done");

		TimeSheetModel timeSheetStatus = new TimeSheetModel();
		LocalDate currentdate = LocalDate.now();
		Month currentMonth = currentdate.getMonth();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime localDateTime = LocalDateTime.now();
		String time = String.valueOf(dateTimeFormatter.format(localDateTime));

		LocalDate localDate = LocalDate.now();

		DateTimeFormatter dateFormat =DateTimeFormatter.ofPattern("dd-MM-yyyy");
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
		ResponseModel responseModel=new ResponseModel();

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDateTime localDateTime = LocalDateTime.now();
		String date = dateTimeFormatter.format(localDateTime);
		TimeSheetModel timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(empId, date);

	   	if (timeSheetModel != null){
  			  if (timeSheetModel.getCheckOut() == null){
				   log.info("You are checked In today and Not checkOut so you are elligible to checkout");	
				   responseModel.setTimeSheetStatus(false);
				   return responseModel;
	    	      }	
			  log.info("you are already checkout for the day");	
			  responseModel.setTimeSheetStatus(null);
			  return responseModel;
		     }
		else{ 
			log.info("You are Not checked In today So you are elligible to Do CheckIn");			
			responseModel.setTimeSheetStatus(true);
			return responseModel;
		   }
	}
	
	
	
	
	//priorTimeaAjustment
	@Override
	public ResponseModel checkPriorStatus(int empId) {
		ResponseModel responseModel=new ResponseModel();

		TimeSheetModel  timeSheetModel;

		List<String> list=new ArrayList<>();
  
		SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
  
        int temp=15;
        
          while(temp>0){ 	   
           	      String date=f.format(cal.getTime());    	   
         	      cal.add(Calendar.DATE,-1);
                  timeSheetModel= timeSheetRepo.findByEmployeeIdAndDate(empId, date);
                    
                    if(timeSheetModel==null)
            	           list.add(date);
           	        else if(timeSheetModel.getCheckOut()==null)
           	     	       list.add(timeSheetModel.toString());

                    temp--;     
              }
          responseModel.setPriorResult(list);
        return responseModel;
	}



	@Override
	public List<TimeSheetModel> empAttendence(int empId, LocalDate fromDate, LocalDate toDate) {
	 String startDate=String.valueOf(fromDate);
	 String endDate=String.valueOf(toDate);
		
     List<TimeSheetModel> list=timeSheetRepo.findAllByEmployeeId(empId,startDate,endDate);
     if(list.isEmpty())
    	 throw new NullPointerException("No attendence data available with given ID");
		
     return list;
	}



	@Override
	public List<TimeSheetModel> allEmpAttendence(LocalDate fromDate, LocalDate toDate) {
     String startDate=String.valueOf(fromDate);
	 String endDate=String.valueOf(toDate);
		
     List<TimeSheetModel> list=timeSheetRepo.findAllByEmployeeId(startDate,endDate);
		return list;	
	}
}
