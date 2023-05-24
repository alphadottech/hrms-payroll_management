package com.alphadot.payroll.service;

import java.text.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.concurrent.TimeUnit;

import com.alphadot.payroll.dto.CheckStatusDTO;
import com.alphadot.payroll.dto.CurrentDateTime;
import com.alphadot.payroll.dto.TimesheetDTO;
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
    public String updateCheckIn(int empId) {
        CurrentDateTime currentDateTime = util.getDateTime();
        Optional<TimeSheetModel> lista = timeSheetRepo.findByEmployeeIdAndDate(empId, currentDateTime.getCurrentDate());
        if (!lista.isPresent()) {
            TimeSheetModel timeSheetModel = new TimeSheetModel();
            timeSheetModel.setDate(currentDateTime.getCurrentDate());
            timeSheetModel.setEmployeeId(empId);
            timeSheetModel.setMonth(String.valueOf(Month.of(currentDateTime.getMonth())));
            timeSheetModel.setCheckIn(currentDateTime.getCurrentTime());
            timeSheetModel.setYear(String.valueOf(currentDateTime.getYear()));
            timeSheetModel.setIntervalStatus(true);
            timeSheetRepo.save(timeSheetModel);
            return "check in Successfully";
        }else {
            TimeSheetModel  timeSheetModel= lista.get();
            if(timeSheetModel.getCheckOut() != null) {
                return "You Are Already Check Out For The Day";
            }
            return "You Are Already Check in";
        }
    }

    @Override
    public String updateCheckOut(int empId) throws ParseException {
        CurrentDateTime currentDateTime = util.getDateTime();
        Optional<TimeSheetModel> timeSheetModelOptional = timeSheetRepo.findByEmployeeIdAndDate(empId, currentDateTime.getCurrentDate());
        if (timeSheetModelOptional.isPresent()) {
            TimeSheetModel timeSheetModel = timeSheetModelOptional.get();
            if (timeSheetModel.getCheckOut() != null) {
                return "You Are Already Check Out";
            }
            timeSheetModel.setCheckOut(currentDateTime.getCurrentTime());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date1 = simpleDateFormat.parse(currentDateTime.getCurrentTime());
            Date date2 = simpleDateFormat.parse(timeSheetModel.getCheckIn());
            long differenceInMilliSeconds = Math.abs(date2.getTime() - date1.getTime());
            long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;
            long differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60;
            long differenceInSeconds = (differenceInMilliSeconds / 1000) % 60;
            timeSheetModel.setWorkingHour(differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds);
            if (timeSheetModel.getLeaveInterval() != null && !timeSheetModel.getLeaveInterval().isEmpty()) {
                if (!timeSheetModel.getIntervalStatus()) {
                    return "Please Resume Your Break";
                }
                String poseResumeInterval = timeSheetModel.getLeaveInterval();
                String arr[] = poseResumeInterval.split(":");
                long inOutDiff = TimeUnit.HOURS.toMillis(differenceInHours)
                        + TimeUnit.MINUTES.toMillis(differenceInMinutes)
                        + TimeUnit.SECONDS.toMillis(differenceInSeconds);

                long poseResumeDiff = TimeUnit.HOURS.toMillis(Integer.parseInt(arr[0]))
                        + TimeUnit.MINUTES.toMillis(Integer.parseInt(arr[1]))
                        + TimeUnit.SECONDS.toMillis(Integer.parseInt(arr[2]));

                long workingMilisecond = inOutDiff - poseResumeDiff;
                long hours = TimeUnit.MILLISECONDS.toHours(workingMilisecond);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(workingMilisecond) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(workingMilisecond) % 60;
                String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                timeSheetModel.setWorkingHour(formattedTime);
            }
            timeSheetModel.setStatus("Present");
            timeSheetModel.setIntervalStatus(false);
            timeSheetRepo.save(timeSheetModel);
            return "Check_Out Successfully";
        }
        return "You Are Not Check in";
    }

    @Override
    public CheckStatusDTO checkStatus(int empId) {
        CurrentDateTime currentDateTime = util.getDateTime();
        Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(empId, currentDateTime.getCurrentDate());
        CheckStatusDTO checkStatusDTO = new CheckStatusDTO();
        if (timeSheetModelData.isPresent()) {
            TimeSheetModel timeSheetModel = timeSheetModelData.get();
            if(timeSheetModel.getCheckIn() != null && timeSheetModel.getWorkingHour() == null && timeSheetModel.getLeaveInterval() == null) {
                checkStatusDTO.setCheckIn(false);
                checkStatusDTO.setCheckOut(true);
                checkStatusDTO.setPause(true);
                checkStatusDTO.setResume(false);
                return checkStatusDTO;
            } else if (timeSheetModel.getCheckIn() != null && timeSheetModel.getCheckOut() == null && !timeSheetModel.getIntervalStatus())  {
                checkStatusDTO.setCheckIn(false);
                checkStatusDTO.setCheckOut(false);
                checkStatusDTO.setPause(false);
                checkStatusDTO.setResume(true);
                return checkStatusDTO;
            } else if (timeSheetModel.getCheckIn() != null && timeSheetModel.getCheckOut() == null && timeSheetModel.getIntervalStatus()) {
                checkStatusDTO.setCheckIn(false);
                checkStatusDTO.setCheckOut(true);
                checkStatusDTO.setPause(false);
                checkStatusDTO.setResume(false);
                return checkStatusDTO;
            } else if (timeSheetModel.getCheckIn() != null && timeSheetModel.getCheckOut() != null) {
                checkStatusDTO.setCheckIn(false);
                checkStatusDTO.setCheckOut(false);
                checkStatusDTO.setPause(false);
                checkStatusDTO.setResume(false);
                return checkStatusDTO;
            }
        }
        checkStatusDTO.setCheckIn(true);
        checkStatusDTO.setCheckOut(false);
        checkStatusDTO.setPause(false);
        checkStatusDTO.setResume(false);
        return checkStatusDTO;
    }

    // priorTimeaAjustment
    @Override
    public ResponseModel checkPriorStatus(int empId) {
        ResponseModel responseModel = new ResponseModel();
        List<String> list = new ArrayList<>();
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        int temp = 15;
        while (temp > 0) {
            String date = f.format(cal.getTime());
            cal.add(Calendar.DATE, -1);
            Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(empId, date);
            if (!timeSheetModelData.isPresent())
                list.add(date);
            else if (timeSheetModelData.get().getCheckOut() == null)
                list.add(timeSheetModelData.get().toString());

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
            Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(
                    priorTimeManagementRequest.getEmployeeId(), priorTimeManagementRequest.getDate());
            priortimeuser.setCheckIn(timeSheetModelData.get().getCheckIn());
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
            Optional<TimeSheetModel> timesheetData = timeSheetRepo.findByEmployeeIdAndDate(employeeId, date);
            if (timesheetData.isPresent()) {
                TimeSheetModel timesheet = timesheetData.get();
                timesheet.setCheckIn(priortime.get().getCheckIn());
                timesheet.setCheckOut(priortime.get().getCheckOut());
                timesheet.setStatus("PRESENT");
                timesheet.setWorkingHour(priortime.get().getWorkingHour());
                return timeSheetRepo.save(timesheet);
            }
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
        return null;
    }


    @Override
    public List<TimesheetDTO> empAttendence(int empId, LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String startDate = String.valueOf(dateTimeFormatter.format(fromDate));
        String endDate = String.valueOf(dateTimeFormatter.format(toDate));
        List<TimeSheetModel> timeSheetModelList = timeSheetRepo.findAllByEmployeeId(empId, startDate, endDate);
        if (timeSheetModelList.isEmpty()) {
            throw new NullPointerException("No attendence data available with given ID: " + empId);
        }
        List<TimesheetDTO> timesheetDTOList = new ArrayList<TimesheetDTO>();
        for (TimeSheetModel timeSheetModel : timeSheetModelList) {
            TimesheetDTO timesheetDTO =  TimesheetDTO.builder()
                    .employeeId(timeSheetModel.getEmployeeId())
                    .date(timeSheetModel.getDate())
                    .checkIn(timeSheetModel.getCheckIn())
                    .checkOut(timeSheetModel.getCheckOut())
                    .workingHour(timeSheetModel.getWorkingHour())
                    .leaveInterval(timeSheetModel.getLeaveInterval())
                    .status(timeSheetModel.getStatus())
                    .build();
            timesheetDTOList.add(timesheetDTO);
        }
        return timesheetDTOList;
    }

    @Override
    public List<TimeSheetModel> allEmpAttendence(LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String startDate = String.valueOf(dateTimeFormatter.format(fromDate));
        String endDate = String.valueOf(dateTimeFormatter.format(toDate));
        List<TimeSheetModel> list = timeSheetRepo.findAllByEmployeeId(startDate, endDate);
        return list;
    }

    @Override
    public String pauseWorkingTime(int empId) {
        CurrentDateTime currentDateTime = util.getDateTime();
        Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(empId, currentDateTime.getCurrentDate());
        if (timeSheetModelData.isPresent()) {
            TimeSheetModel timeSheetModel = timeSheetModelData.get();
            if ((timeSheetModel.getCheckIn() != null && !timeSheetModel.getCheckIn().isEmpty()) && (timeSheetModel.getCheckOut() == null)) {
                timeSheetModel.setLeaveInterval(currentDateTime.getCurrentTime());
                timeSheetModel.setIntervalStatus(false);
                timeSheetRepo.save(timeSheetModel);
                return "Working TIME Pause Successfully";
            }
            return "Already Check OUT For The Day";
        }
        return "Please Check in First";
    }

    @Override
    public String resumeWorkingTime(int empId) throws ParseException {
        CurrentDateTime currentDateTime = util.getDateTime();
        Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(empId, currentDateTime.getCurrentDate());
        if (timeSheetModelData.isPresent()) {
            TimeSheetModel timeSheetModel = timeSheetModelData.get();
            if (timeSheetModel.getCheckOut() == null) {
                if (timeSheetModel.getLeaveInterval() != null && !timeSheetModel.getLeaveInterval().isEmpty()) {
                    DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
                    Date poseTime = timeFormat.parse(timeSheetModel.getLeaveInterval());
                    Date resumeTime = timeFormat.parse(currentDateTime.getCurrentTime());
                    long differenceInMilliSeconds = Math.abs(poseTime.getTime() - resumeTime.getTime());
                    long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;
                    long differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60;
                    long differenceInSeconds = (differenceInMilliSeconds / 1000) % 60;
                    timeSheetModel.setLeaveInterval(differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds);
                    timeSheetModel.setIntervalStatus(true);
                    timeSheetRepo.save(timeSheetModel);
                    return "Working TIME Resume Successfully";
                }
                return "Please Pose Working TIME";
            }
            return "Already Check OUT For The Day";
        }
        return "Please Check in First";
    }
}
