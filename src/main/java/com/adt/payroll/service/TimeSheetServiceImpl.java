package com.adt.payroll.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.adt.payroll.dto.CheckStatusDTO;
import com.adt.payroll.dto.CurrentDateTime;
import com.adt.payroll.dto.EmployeeExpenseDTO;
import com.adt.payroll.dto.TimesheetDTO;
import com.adt.payroll.event.OnPriorTimeDetailsSavedEvent;
import com.adt.payroll.exception.NoDataFoundException;
import com.adt.payroll.service.Helper;
import com.adt.payroll.model.EmployeeExpense;
import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.model.OnLeaveRequestSaveEvent;
import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.User;
import com.adt.payroll.model.payload.PriorTimeManagementRequest;
import com.adt.payroll.msg.ResponseModel;
import com.adt.payroll.repository.EmployeeExpenseRepo;
import com.adt.payroll.repository.PriorTimeRepository;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.repository.UserRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TimeSheetServiceImpl implements TimeSheetService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TimeSheetRepo timeSheetRepo;

    @Autowired
    private PriorTimeRepository priorTimeRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TableDataExtractor dataExtractor;


    @Autowired
    private EmployeeExpenseRepo employeeExpenseRepo;
    
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    public void updateDaysForPresentStatus() {
        timeSheetRepo.updateDaysForPresentEntries();
    }

    @Value("${Expenses_Invoice_Path}")
    private String invoicePath;

    @Autowired
    private Util util;

    @Value("${latitude}")
    private double COMPANY_LATITUDE;

    @Value("${longitude}")
    private double COMPANY_LONGITUDE;

    @Value("${MAX_DISTANCE_THRESHOLD}")
    private double MAX_DISTANCE_THRESHOLD;
    
    @Value("${-Dmy.port}")
   	private String serverPort;

   	@Value("${-Dmy.property}")
   	private String ipaddress;
   	
   	@Value("${-UI.scheme}")
   	private String scheme;

   	@Value("${-UI.context}")
   	private String context;

    @Override
    public String updateCheckIn(int empId, double latitude, double longitude) {
        double distance = calculateDistance(latitude, longitude, COMPANY_LATITUDE, COMPANY_LONGITUDE);

        CurrentDateTime currentDateTime = util.getDateTime();
        LOGGER.info("currentDateTimeObj" + currentDateTime);
        Optional<TimeSheetModel> timeSheetModels = timeSheetRepo.findByEmployeeIdAndDate(empId,
                currentDateTime.getCurrentDate());
        if (!timeSheetModels.isPresent()) {
            TimeSheetModel timeSheetModel = new TimeSheetModel();
            timeSheetModel.setDate(currentDateTime.getCurrentDate());
            timeSheetModel.setEmployeeId(empId);
            timeSheetModel.setMonth(String.valueOf(Month.of(currentDateTime.getMonth())));
            timeSheetModel.setCheckIn(currentDateTime.getCurrentTime());
            timeSheetModel.setYear(String.valueOf(currentDateTime.getYear()));
            timeSheetModel.setIntervalStatus(true);

            LocalDate date = LocalDate.parse(currentDateTime.getCurrentDate(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            timeSheetModel.setDay(dayOfWeek.getDisplayName(
                    TextStyle.FULL, Locale.ENGLISH));

            if (distance >= MAX_DISTANCE_THRESHOLD) {
                timeSheetModel.setCheckInLatitude(String.valueOf(latitude));
                timeSheetModel.setCheckInLongitude(String.valueOf(longitude));
                timeSheetModel.setCheckInDistance(String.valueOf(distance));
                timeSheetModel.setCheckInDistanceStatus("checkedIn out of office");
                timeSheetRepo.save(timeSheetModel);
                return " you have check in with latitude: " + latitude + " and longitude: " + longitude + ", which are not within office covered distance";
            }
            timeSheetModel.setCheckInLatitude(String.valueOf(latitude));
            timeSheetModel.setCheckInLongitude(String.valueOf(longitude));
            timeSheetModel.setCheckInDistance(String.valueOf(distance));
            timeSheetModel.setCheckInDistanceStatus("checkedIn from office");
            LOGGER.info(currentDateTime.getCurrentTime());
            timeSheetRepo.save(timeSheetModel);
            return "you have check in with latitude: " + latitude + " and longitude: " + longitude;
        } else {
            TimeSheetModel timeSheetModel = timeSheetModels.get();
            if (timeSheetModel.getCheckOut() != null) {
                return "You Are Already Check Out For The Day";
            }
            return "You Are Already Check in";
        }

    }

    @Override
    public String updateCheckOut(int empId, double latitude, double longitude) throws ParseException {
        double distance = calculateDistance(latitude, longitude, COMPANY_LATITUDE, COMPANY_LONGITUDE);

        CurrentDateTime currentDateTime = util.getDateTime();
        Optional<TimeSheetModel> timeSheetModelOptional = timeSheetRepo.findByEmployeeIdAndDate(empId,
                currentDateTime.getCurrentDate());
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
            timeSheetModel
                    .setWorkingHour(differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds);
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

            if (distance >= MAX_DISTANCE_THRESHOLD) {
                timeSheetModel.setCheckOutLatitude(String.valueOf(latitude));
                timeSheetModel.setCheckOutLongitude(String.valueOf(longitude));
                timeSheetModel.setCheckOutDistance(String.valueOf(distance));
                timeSheetModel.setCheckOutDistanceStatus("checkedOut out of office");
                timeSheetRepo.save(timeSheetModel);
                return " you have Check out with latitude: " + latitude + " and longitude: " + longitude + ", which are not within office covered distance";
            }
            timeSheetModel.setEarlyCheckOutStatus(false);
            timeSheetModel.setCheckOutLatitude(String.valueOf(latitude));
            timeSheetModel.setCheckOutLongitude(String.valueOf(longitude));
            timeSheetModel.setCheckOutDistance(String.valueOf(distance));
            timeSheetModel.setCheckOutDistanceStatus("checkedOut from office");
            timeSheetRepo.save(timeSheetModel);
            return "you have Check out with latitude: " + latitude + " and longitude: " + longitude;
        }
        return "You Are Not Check in";

    }

    @Override
    public CheckStatusDTO checkStatus(int empId) {
        CurrentDateTime currentDateTime = util.getDateTime();
        Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(empId,
                currentDateTime.getCurrentDate());
        CheckStatusDTO checkStatusDTO = new CheckStatusDTO();
        if (timeSheetModelData.isPresent()) {
            TimeSheetModel timeSheetModel = timeSheetModelData.get();
            if (timeSheetModel.getCheckIn() != null && timeSheetModel.getWorkingHour() == null
                    && timeSheetModel.getLeaveInterval() == null) {
                checkStatusDTO.setCheckIn(false);
                checkStatusDTO.setCheckOut(true);
                checkStatusDTO.setPause(true);
                checkStatusDTO.setResume(false);
                return checkStatusDTO;
            } else if (timeSheetModel.getCheckIn() != null && timeSheetModel.getCheckOut() == null
                    && !timeSheetModel.getIntervalStatus()) {
                checkStatusDTO.setCheckIn(false);
                checkStatusDTO.setCheckOut(false);
                checkStatusDTO.setPause(false);
                checkStatusDTO.setResume(true);
                return checkStatusDTO;
            } else if (timeSheetModel.getCheckIn() != null && timeSheetModel.getCheckOut() == null
                    && timeSheetModel.getIntervalStatus()) {
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
    public List<ResponseModel> checkPriorStatus(int empId) {
    	LOGGER.info("getPriorTimeData");
        TimeSheetModel timeSheetModel = new TimeSheetModel();
        List<ResponseModel> list = new ArrayList<>();
        User use = userRepo.getById(empId);
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateformater = new SimpleDateFormat("yyyy-MM-dd");  
        Calendar cal = Calendar.getInstance();
        Calendar calender = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        calender.add(Calendar.DATE, -15);
        String from = dateformater.format(cal.getTime());
        String to = dateformater.format(calender.getTime());
        try {
        String leaveSql ="SELECT ld.leavedate FROM payroll_schema.leave_dates ld JOIN payroll_schema.leave_request lr ON lr.leaveid = ld.leave_id WHERE lr.empid = "+empId+"  AND ld.leavedate BETWEEN "+"'"+to+"'"+" AND "+"'"+from+"'";
        LOGGER.info(leaveSql);
        List<Map<String, Object>> leaveData = dataExtractor.extractDataFromTable(leaveSql);
        List<String> listOfLeaveDate = new ArrayList<>();
        for (Map<String, Object> leave : leaveData) {
        	listOfLeaveDate.add(String.valueOf(leave.get("leavedate")));
        }
        String sql = "select * from employee_schema.holiday";
        LOGGER.info(sql);
        List<Map<String, Object>> tableData = dataExtractor.extractDataFromTable(sql);
        List<String> listOfDate = new ArrayList<>();
        for (Map<String, Object> holiday : tableData) {
            listOfDate.add(String.valueOf(holiday.get("date")));
        }
        int temp = 15;
        boolean checkDay;
        while (temp > 0) {
            checkDay = true;
            ResponseModel responseModel = new ResponseModel();
            responseModel.setEmployeeId(empId);
            String checkDate = dateformater.format(cal.getTime());
            LOGGER.info("checkDate"+checkDate);
            int dayNumber = cal.get(Calendar.DAY_OF_MONTH);
            String dayNames[] = new DateFormatSymbols().getWeekdays();
            String nameDay = dayNames[cal.get(Calendar.DAY_OF_WEEK)];
            if ((nameDay.equalsIgnoreCase(Util.SATURDAY))
                    && ((dayNumber >= 8 && dayNumber <= 14) || (dayNumber >= 22 && dayNumber <= 28))
                    || (nameDay.equalsIgnoreCase(Util.SUNDAY)) || (listOfDate.contains(checkDate))||(listOfLeaveDate.contains(checkDate))) {
                checkDay = false;
            }
            String date = f.format(cal.getTime());
            cal.add(Calendar.DATE, -1);
            if (checkDay) {
                responseModel.setStatus("Pending");
                responseModel.setDate(date);
                responseModel.setEmail(use.getEmail());
                LOGGER.info("Get last 15 days CheckIn and CheckOut Data");
                Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(empId, date);
                LOGGER.info("Present priortime date"+checkDate);
                LOGGER.info(""+timeSheetModelData);
                if (!timeSheetModelData.isPresent()) {
                    LOGGER.info("checkIn and checkout data not present in timesheet ");
                    list.add(responseModel);
                } else if (timeSheetModelData.get().getCheckOut() == null && !timeSheetModelData.isEmpty()) {
                    LOGGER.info("checkIn and checkout data present in timesheet ");
                    responseModel.setCheckIn(timeSheetModelData.get().getCheckIn());
                    responseModel.setStatus("Pending");
                    responseModel.setWorkingHour(timeSheetModelData.get().getWorkingHour());
                    responseModel.setCheckOut(timeSheetModelData.get().getCheckOut());
                    responseModel.setDate(timeSheetModelData.get().getDate());
                    responseModel.setEmail(use.getEmail());
                    list.add(responseModel);
                }

				Optional<Priortime> priortime = priorTimeRepository.findByEmployeeIdAndDate(empId, date);
                LOGGER.info("Pending Priortime Data");
				if (priortime.isPresent()) {
					Priortime prior = priortime.get();
					long currentTime = System.currentTimeMillis();
					if (priortime.get().getExpiryTime() <= currentTime
							&& !prior.getStatus().equalsIgnoreCase("Accepted")) {
						responseModel.setStatus("Resend");
						prior.setStatus("Resend");
						priorTimeRepository.save(prior);
					}
				}

			}
			temp--;
		}
        }catch(Exception e) {
        	LOGGER.error(e.getMessage());
        }
        LOGGER.info("getAllDataSuccessfully return"+list);
        return list;
    }

    public Optional<Priortime> savePriorTime(PriorTimeManagementRequest priorTimeManagementRequest, double latitude, double longitude)
            throws ParseException {
        Priortime priortimeuser = new Priortime();
        double distance = calculateDistance(latitude, longitude, COMPANY_LATITUDE, COMPANY_LONGITUDE);
        Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(
                priorTimeManagementRequest.getEmployeeId(), priorTimeManagementRequest.getDate());
        if (!timeSheetModelData.isPresent()) {
            priortimeuser.setCheckInLatitude(String.valueOf(latitude));
            priortimeuser.setCheckInLongitude(String.valueOf(longitude));
            priortimeuser.setCheckOutLatitude(String.valueOf(latitude));
            priortimeuser.setCheckOutLongitude(String.valueOf(longitude));
            priortimeuser.setCheckInDistance(String.valueOf(distance));
            priortimeuser.setCheckOutDistance(String.valueOf(distance));
        } else {
            priortimeuser.setCheckOutLatitude(String.valueOf(latitude));
            priortimeuser.setCheckOutLongitude(String.valueOf(longitude));
            priortimeuser.setCheckOutDistance(String.valueOf(distance));
            priortimeuser.setCheckInLatitude(timeSheetModelData.get().getCheckInLatitude());
            priortimeuser.setCheckInLongitude(timeSheetModelData.get().getCheckInLongitude());
            priortimeuser.setCheckInDistance(timeSheetModelData.get().getCheckInDistance());
        }
        priortimeuser.setDate(priorTimeManagementRequest.getDate());
        priortimeuser.setEmail(priorTimeManagementRequest.getEmail());
        priortimeuser.setEmployeeId(priorTimeManagementRequest.getEmployeeId());
        priortimeuser.setStatus(priorTimeManagementRequest.getStatus());
        priortimeuser.setCheckIn(priorTimeManagementRequest.getCheckIn());
        priortimeuser.setCheckOut(priorTimeManagementRequest.getCheckOut());
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat monthFormatter = new SimpleDateFormat("MMMM");
        Date d = dateFormatter.parse(String.valueOf(priorTimeManagementRequest.getDate()));
        String month = monthFormatter.format(d);
        SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");
        Date y = dateFormatter.parse(String.valueOf(priorTimeManagementRequest.getDate()));
        String year = yearFormatter.format(y);
        DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        Date checkin = timeFormat.parse(priorTimeManagementRequest.getCheckIn());
        Date checkout = timeFormat.parse(priorTimeManagementRequest.getCheckOut());
        long differenceInMilliSeconds = Math.abs(checkin.getTime() - checkout.getTime());
        long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;
        long differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60;
        long differenceInSeconds = (differenceInMilliSeconds / 1000) % 60;
        priortimeuser.setWorkingHour(differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds);
        priortimeuser.setMonth(month.toUpperCase());
        priortimeuser.setYear(year.toUpperCase());
        long millisecondsInFiveDays = TimeUnit.DAYS.toMillis(5);
		long currentTime=System.currentTimeMillis();
        priortimeuser.setExpiryTime(currentTime+millisecondsInFiveDays);
        Priortime priortime = priorTimeRepository.save(priortimeuser);
        return Optional.ofNullable(priortime);
    }

    public TimeSheetModel saveConfirmedDetails(Optional<Priortime> priortime) throws ParseException {
        Integer employeeId = priortime.get().getEmployeeId();
        String date = priortime.get().getDate();
        Optional<TimeSheetModel> timesheetData = timeSheetRepo.findByEmployeeIdAndDate(employeeId, date);
        if (timesheetData.isPresent()) {
            TimeSheetModel timesheet = timesheetData.get();
            timesheet.setCheckIn(priortime.get().getCheckIn());
            timesheet.setCheckOut(priortime.get().getCheckOut());
            timesheet.setStatus("Present");
            timesheet.setWorkingHour(priortime.get().getWorkingHour());
            timesheet.setCheckOutLatitude(priortime.get().getCheckOutLatitude());
            timesheet.setCheckOutLongitude(priortime.get().getCheckOutLongitude());
            timesheet.setCheckOutDistance(priortime.get().getCheckOutDistance());
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
            timesheet.setStatus("Present");
            timesheet.setCheckOutLatitude(priortime.get().getCheckOutLatitude());
            timesheet.setCheckOutLongitude(priortime.get().getCheckOutLongitude());
            timesheet.setCheckOutDistance(priortime.get().getCheckOutDistance());
            timesheet.setCheckInLatitude(priortime.get().getCheckInLatitude());
            timesheet.setCheckInLongitude(priortime.get().getCheckInLongitude());
            timesheet.setCheckInDistance(priortime.get().getCheckInDistance());
            return timeSheetRepo.save(timesheet);
        }


    }

    @Override
    public List<TimesheetDTO> empAttendence(int empId, LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String startDate = String.valueOf(dateTimeFormatter.format(fromDate));
        String endDate = String.valueOf(dateTimeFormatter.format(toDate));
        List<TimeSheetModel> timeSheetModelList = timeSheetRepo.findAllByEmployeeIdWithinSpecifiedDateRange(empId, startDate, endDate);
        if (timeSheetModelList.isEmpty()) {
            throw new NoDataFoundException("No attendance data available with given ID: " + empId);
        }
        List<TimesheetDTO> timesheetDTOList = new ArrayList<TimesheetDTO>();
        for (TimeSheetModel timeSheetModel : timeSheetModelList) {
            TimesheetDTO timesheetDTO = TimesheetDTO.builder().employeeId(timeSheetModel.getEmployeeId())
                    .date(timeSheetModel.getDate()).checkIn(timeSheetModel.getCheckIn())
                    .checkOut(timeSheetModel.getCheckOut()).workingHour(timeSheetModel.getWorkingHour())
                    .leaveInterval(timeSheetModel.getLeaveInterval()).status(timeSheetModel.getStatus()).build();
            timesheetDTOList.add(timesheetDTO);
        }
        return timesheetDTOList;
    }

    @Override
    public List<TimeSheetModel> allEmpAttendence(LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String startDate = dateTimeFormatter.format(fromDate);
        String endDate = dateTimeFormatter.format(toDate);
        List<TimeSheetModel> list = timeSheetRepo.findAllWithinSpecifiedDateRange(startDate, endDate).stream()
                .filter(e -> e != null)
                .map(e -> {
                    Optional<User> t = userRepo.findById(e.getEmployeeId());
                    if (t.isPresent()) {
                        e.setEmployeeName(t.get().getFirstName() + " " + t.get().getLastName());
                    } else {
                        e.setEmployeeName("NOT AVAILABLE");
                    }
                    // Set the day of the week
                    e.setDayOfWeek(e.getDayOfWeek());
                    return e;
                }).collect(Collectors.toList());
        return list;
    }


    @Override
    public String pauseWorkingTime(int empId) {
        CurrentDateTime currentDateTime = util.getDateTime();
        Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(empId,
                currentDateTime.getCurrentDate());
        if (timeSheetModelData.isPresent()) {
            TimeSheetModel timeSheetModel = timeSheetModelData.get();
            if ((timeSheetModel.getCheckIn() != null && !timeSheetModel.getCheckIn().isEmpty())
                    && (timeSheetModel.getCheckOut() == null)) {
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
        Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(empId,
                currentDateTime.getCurrentDate());
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
                    timeSheetModel.setLeaveInterval(
                            differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds);
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

    @Override
    public EmployeeExpenseDTO employeeExpense(int empId, List<MultipartFile> image, EmployeeExpense employeeExpense)
            throws IOException {
        Optional<User> employeeOptional = userRepo.findById(empId);
        if (employeeOptional.isPresent()) {
            employeeExpense.setEmployeeId(empId + "");
            EmployeeExpenseDTO employeeExpenseDTO = new EmployeeExpenseDTO();
            if (!image.isEmpty()) {
                if (!image.get(0).isEmpty()) {
                    String invoice = convertMultipartToFile(image, employeeExpenseDTO);
                    employeeExpense.setInvoices(invoice);
                }
            }
            CurrentDateTime currentDateTime = util.getDateTime();
            employeeExpense.setSubmitDate(currentDateTime.getCurrentDate());
            employeeExpense.setStatus("Pending");
            EmployeeExpense employeeExpenses = employeeExpenseRepo.save(employeeExpense);
            User employee = employeeOptional.get();
            changeToDTO(employeeExpenses, employeeExpenseDTO, employee);
            return employeeExpenseDTO;
        } else {
            String message = "Employee id: " + empId + " is not exists";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    public static EmployeeExpenseDTO changeToDTO(EmployeeExpense employeeExpense, EmployeeExpenseDTO employeeExpenseDTO,
                                                 User employee) {
        employeeExpenseDTO.setExpenseId(employeeExpense.getExpenseId());
        employeeExpenseDTO.setEmployeeId(employeeExpense.getEmployeeId());
        employeeExpenseDTO.setSubmitDate(employeeExpense.getSubmitDate());
        employeeExpenseDTO.setExpenseAmount(employeeExpense.getExpenseAmount());
        employeeExpenseDTO.setEmpName(employee.getFirstName() + " " + employee.getLastName());
        employeeExpenseDTO.setEmpEmail(employee.getEmail());
        employeeExpenseDTO.setExpenseCategory(employeeExpense.getExpenseCategory());
        employeeExpenseDTO.setExpenseDescription(employeeExpense.getExpenseDescription());
        employeeExpenseDTO.setEmployeeComments(employeeExpense.getEmployeeComments());
        employeeExpenseDTO.setPaymentDate(employeeExpense.getPaymentDate());
        employeeExpenseDTO.setPaymentMode(employeeExpense.getPaymentMode());
        employeeExpenseDTO.setEmployeeComments(employeeExpense.getEmployeeComments());
        return employeeExpenseDTO;
    }

    public String convertMultipartToFile(List<MultipartFile> multipartFileList, EmployeeExpenseDTO employeeExpenseDTO)
            throws IOException {
        HashMap<String, File> map = new HashMap<>();
        StringBuilder fileLinks = new StringBuilder(); // StringBuilder to store file links
        for (MultipartFile multipartFile : multipartFileList) {
            if (!multipartFile.isEmpty()) {
                try {
                    byte[] bytes = multipartFile.getBytes();
                    File file = new File(invoicePath + multipartFile.getOriginalFilename());
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(bytes);
                    outputStream.close();
                    map.put(file.getName(), file);
                    fileLinks.append(file.getAbsolutePath()).append(",");
                } catch (Exception exception) {
                    String message = "File is not exists in path: " + invoicePath;
                    LOGGER.error(message);
                    throw new RuntimeException(message);
                }
            }
        }
        if (fileLinks.length() > 0) {
            fileLinks.deleteCharAt(fileLinks.length() - 1);
        }
        employeeExpenseDTO.setInvoices(map);
        return fileLinks.toString();
    }

    @Override
    public EmployeeExpenseDTO acceptedEmployeeExpense(int expenseId, EmployeeExpenseDTO employeeExpenseDTO)
            throws IOException {
        Optional<EmployeeExpense> employeeExpenseOptional = employeeExpenseRepo.findById(expenseId);
//	        EmployeeExpenseDTO employeeExpenseDTO = new EmployeeExpenseDTO();
        if (employeeExpenseOptional.isPresent()) {
            EmployeeExpense employeeExpense = employeeExpenseOptional.get();
            if (employeeExpenseDTO.getStatus() == null) {
                String message = "Please submit the employee expense status of ExpenseId:" + expenseId;
                LOGGER.error("Please submit the employee expense status of ExpenseId: {}", expenseId);
                throw new RuntimeException(message);
            }
            employeeExpense.setStatus(employeeExpenseDTO.getStatus());
            employeeExpense.setPayrollComments(employeeExpenseDTO.getPayrollComments());
            EmployeeExpense employeeExpense1 = employeeExpenseRepo.save(employeeExpense);
            return employeeExpenseDTO;
        } else {
            String message = "Expense id: " + expenseId + " does not exists";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    public File getFileFromPath(String filePath) {
        File file = new File(filePath);
        try {
            FileInputStream in = new FileInputStream(file);
            long size = file.length();
            byte[] temp = new byte[(int) size];
            in.read(temp);
            in.close();

        } catch (Exception ex) {
            String message = "File not found in following path: " + filePath;
            LOGGER.error("File not found in following path: {}", filePath);
            throw new RuntimeException(message);
        }
        return file;
    }

    @Override
    public String approveEmployeeExpenseById(int expenseId) {
        EmployeeExpense employeeExpense = employeeExpenseRepo.findByExpenseId(expenseId);
        if (employeeExpense.getStatus().equalsIgnoreCase("pending")) {
            employeeExpense.setStatus("Approve");
            employeeExpenseRepo.save(employeeExpense);
            return "Approved Successfully";
        } else {
            return " status Already updated";
        }
    }

    @Override
    public String rejectEmployeeExpenseById(int expenseId) {
        EmployeeExpense employeeExpense = employeeExpenseRepo.findByExpenseId(expenseId);
        if (employeeExpense.getStatus().equalsIgnoreCase("pending")) {
            employeeExpense.setStatus("Reject");
            employeeExpenseRepo.save(employeeExpense);
            return "Reject Successfully";
        } else {
            return " status Already updated";
        }
    }

    @Override
    public List<EmployeeExpense> getAllExpenseDetail() {
        return employeeExpenseRepo.findAll();

    }


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Implementation of Haversine formula or other distance calculation algorithm
        // Calculate and return the distance between two coordinates

        // Example Haversine formula implementation:
        double earthRadius = 6371000; // Radius of the Earth in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        return distance;
    }

    @Override
    public ByteArrayInputStream getExcelData(LocalDate fromtDate, LocalDate toDate) throws IOException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String startDate = String.valueOf(dateTimeFormatter.format(fromtDate));
        String endDate = String.valueOf(dateTimeFormatter.format(toDate));
        List<TimeSheetModel> specifiedDateRangeData = timeSheetRepo.findAllWithinSpecifiedDateRange(startDate, endDate);
        Map<Integer, String> employeeNames = fetchEmployeeNames(specifiedDateRangeData);

        for (TimeSheetModel timeSheet : specifiedDateRangeData) {
            String employeeName = employeeNames.get(timeSheet.getEmployeeId());
            timeSheet.setEmployeeName(employeeName);
        }
        return Helper.dataToExcel(specifiedDateRangeData);
    }

    private Map<Integer, String> fetchEmployeeNames(List<TimeSheetModel> timeSheets) {
        Set<Integer> employeeIds = timeSheets.stream()
                .map(TimeSheetModel::getEmployeeId)
                .collect(Collectors.toSet());

        // Fetch employee names based on employeeIds
        Map<Integer, String> employeeNames = new HashMap<>();
        for (Integer employeeId : employeeIds) {
            Optional<User> t = userRepo.findById(employeeId);
            if (t.isPresent()) {
                String newemp = t.get().getFirstName() + " " + t.get().getLastName();
                employeeNames.put(employeeId, newemp);
            }
        }
        return employeeNames;

    }
    
    public  String reSendPriorTimeRequest(int priortimeId) {
    Optional<Priortime>  priorTimeRequest= priorTimeRepository.findById(priortimeId);
	if (priorTimeRequest.isPresent()) {
		Priortime priortime=priorTimeRequest.get();
		priortime.setStatus("Pending");
		long millisecondsInFiveDays = TimeUnit.DAYS.toMillis(5);
   		long currentTime=System.currentTimeMillis();
   		priortime.setExpiryTime(currentTime+millisecondsInFiveDays);
   	  UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.newInstance()
				.scheme(scheme)
				.host(ipaddress)
				.port(serverPort)
				.path(context+"/payroll/timeSheet/updatePriorTime/Accepted/" + priortimeId);              
      UriComponentsBuilder urlBuilder2 = ServletUriComponentsBuilder.newInstance()
				.scheme(scheme)
				.host(ipaddress)
				.port(serverPort)
				.path(context+"/payroll/timeSheet/updatePriorTime/Rejected/" + priortimeId);
      OnPriorTimeDetailsSavedEvent onPriorTimeDetailsSavedEvent = new OnPriorTimeDetailsSavedEvent(priortime,
              urlBuilder1, urlBuilder2);
      applicationEventPublisher.publishEvent(onPriorTimeDetailsSavedEvent);	
       priorTimeRepository.save(priortime);
      return "reSend email successfully";
	}
   
   	return "this records  not persent "; 
   	 
    }
    
public String checkInCheckOutForContractBasedEmployee(String workingHours, String date,double latitude,double longitude,int empId) {
  double distance = calculateDistance(latitude, longitude, COMPANY_LATITUDE, COMPANY_LONGITUDE);
  Optional<TimeSheetModel>	timeSheet=timeSheetRepo.findByEmployeeIdAndDate(empId,date);
	if (!timeSheet.isPresent()) {
		LocalDate dt = LocalDate.parse(date);
		TimeSheetModel timeSheetData = new TimeSheetModel();
		timeSheetData.setEmployeeId(empId);
		timeSheetData.setMonth(dt.getMonth().toString());
		timeSheetData.setYear("" + dt.getYear());
		timeSheetData.setWorkingHour(workingHours+":00");
		timeSheetData.setDate(date);
		timeSheetData.setCheckInLatitude(Double.toString(latitude));
		timeSheetData.setCheckInLongitude(Double.toString(longitude));
		timeSheetData.setCheckInDistance(Double.toString(distance));
		timeSheetData.setCheckOutDistance(Double.toString(distance));
		timeSheetData.setCheckOutLatitude(Double.toString(latitude));
		timeSheetData.setCheckOutLongitude(Double.toString(latitude));
		timeSheetData.setStatus("Present");
		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime checkInTime = currentDateTime.withHour(10).withMinute(00).withSecond(0).withNano(0);
		String checkin = checkInTime.format(formatter);
		timeSheetData.setCheckIn(checkin);
		String[] arrayOfHours = workingHours.split(":");
		String hrs = arrayOfHours[0];
		String mnt = arrayOfHours[1];
		int minute = Integer.parseInt(mnt);
		int hour = Integer.parseInt(hrs);
		hour=hour + 10;
		LocalDateTime checkOutTime = checkInTime.withHour(hour).withMinute(minute).withSecond(00).withNano(0);
		String checkOut = checkOutTime.format(formatter);
		timeSheetData.setCheckOut(checkOut);
		timeSheetRepo.save(timeSheetData);
		 return "TimeSheet data has been submitted successfully";
	}else {

	}
	return "TimeSheet data already present for the selected date";

}

@Override
public String earlyCheckOut(double latitude, double longitude, int empId,String reason, String reasonType) throws ParseException {
	   double distance = calculateDistance(latitude, longitude, COMPANY_LATITUDE, COMPANY_LONGITUDE);
	   CurrentDateTime currentDateTime = util.getDateTime();
       Optional<TimeSheetModel> timeSheetModelOptional = timeSheetRepo.findByEmployeeIdAndDate(empId,
               currentDateTime.getCurrentDate());
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
           timeSheetModel
                   .setWorkingHour(differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds);
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

           if (distance >= MAX_DISTANCE_THRESHOLD) {
               timeSheetModel.setCheckOutLatitude(String.valueOf(latitude));
               timeSheetModel.setCheckOutLongitude(String.valueOf(longitude));
               timeSheetModel.setCheckOutDistance(String.valueOf(distance));
               timeSheetRepo.save(timeSheetModel);
               return " you have Check out with latitude: " + latitude + " and longitude: " + longitude + ", which are not within office covered distance";
           }
           timeSheetModel.setReasonType(reasonType);
           timeSheetModel.setReason(reason);
           timeSheetModel.setEarlyCheckOutStatus(true);
           timeSheetModel.setCheckOutLatitude(String.valueOf(latitude));
           timeSheetModel.setCheckOutLongitude(String.valueOf(longitude));
           timeSheetModel.setCheckOutDistance(String.valueOf(distance));
           timeSheetRepo.save(timeSheetModel);
           return "you have Check out with latitude: " + latitude + " and longitude: " + longitude;
       }
       return "You Are Not Check in";

}



}