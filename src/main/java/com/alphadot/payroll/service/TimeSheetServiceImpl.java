package com.alphadot.payroll.service;

import java.text.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.Optional;

import com.alphadot.payroll.dto.CurrentDateTime;
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

    @Autowired
    private Util util;

    @Override
    public ResponseModel updateCheckIn(int id) {
        TimeSheetModel timeSheetModel = new TimeSheetModel();
        CurrentDateTime currentDateTime = util.getDateTime();
        timeSheetModel.setDate(currentDateTime.getCurrentDate());
        timeSheetModel.setEmployeeId(id);
        timeSheetModel.setMonth(String.valueOf(currentDateTime.getMonth()));
        timeSheetModel.setCheckIn(currentDateTime.getCurrentTime());
        timeSheetModel.setYear(String.valueOf(currentDateTime.getYear()));
        timeSheetRepo.save(timeSheetModel);
        log.info("successfully done checkIn and returning to controller");
        ResponseModel responseModel = new ResponseModel();
        responseModel.setMsg("check In successfully AT :" + currentDateTime.getCurrentTime());
        return responseModel;
    }

    @Override
    public ResponseModel updateCheckOut(int id) {
        ResponseModel responseModel = new ResponseModel();
        CurrentDateTime currentDateTime = util.getDateTime();
        TimeSheetModel timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(id, currentDateTime.getCurrentDate());
        try {
            timeSheetModel.setCheckOut(currentDateTime.getCurrentTime());
            timeSheetModel.setMonth(String.valueOf(currentDateTime.getMonth()));
            timeSheetModel.setYear(String.valueOf(currentDateTime.getYear()));
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
            responseModel.setMsg(" checkout successfully AT :" + currentDateTime.getCurrentTime());
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
        CurrentDateTime currentDateTime = util.getDateTime();
        TimeSheetModel timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(empId, currentDateTime.getCurrentDate());
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
        TimeSheetModel timeSheetModel;
        List<String> list = new ArrayList<>();
//        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
        CurrentDateTime currentDateTime = util.getDateTime();
//        Calendar cal = Calendar.getInstance();
        int temp = 15;
        while (temp > 0) {
//            String date = f.format(cal.getTime());
//            cal.add(Calendar.DATE, -1);
            timeSheetModel = timeSheetRepo.findByEmployeeIdAndDate(empId, currentDateTime.getCurrentDate());
            if (timeSheetModel == null)
                list.add(currentDateTime.getCurrentDate());
            else if (timeSheetModel.getCheckOut() == null)
                list.add(timeSheetModel.toString());

            temp--;
        }
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPriorResult(list);
        return responseModel;
    }

    public Optional<Priortime> savePriorTime(PriorTimeManagementRequest priorTimeManagementRequest)
            throws ParseException {

        Priortime priortimeuser = new Priortime();
        if (priorTimeManagementRequest.getCheckIn() != null && !priorTimeManagementRequest.getCheckIn().equals("")) {
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
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDateTime = LocalDate.parse(priorTimeManagementRequest.getDate(), dateTimeFormatter);
        Month m = localDateTime.getMonth();
        priortimeuser.setMonth(m.toString());

        SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");
        Date y = dateFormatter.parse(String.valueOf(priorTimeManagementRequest.getDate()));
        String year = yearFormatter.format(y);
        priortimeuser.setYear(year.toUpperCase());
        DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        Date checkin = timeFormat.parse(priortimeuser.getCheckIn());
        Date checkout = timeFormat.parse(priortimeuser.getCheckOut());
        long differenceInMilliSeconds = Math.abs(checkin.getTime() - checkout.getTime());
        long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;
        long differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60;
        long differenceInSeconds = (differenceInMilliSeconds / 1000) % 60;
        priortimeuser.setWorkingHour(differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds);

        Priortime priortime = priorTimeRepository.save(priortimeuser);

        return Optional.ofNullable(priortime);
    }


    public TimeSheetModel saveConfirmedDetails(Optional<Priortime> priortime) throws ParseException {
        Integer employeeId = priortime.get().getEmployeeId();
        String date = priortime.get().getDate();
        TimeSheetModel timesheet = timeSheetRepo.findByEmployeeIdAndDate(employeeId, date);
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
