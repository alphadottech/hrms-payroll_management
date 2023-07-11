package com.adt.payroll.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.adt.payroll.dto.CheckStatusDTO;
import com.adt.payroll.dto.CurrentDateTime;
import com.adt.payroll.dto.EmployeeExpenseDTO;
import com.adt.payroll.dto.TimesheetDTO;
import com.adt.payroll.model.Employee;
import com.adt.payroll.model.EmployeeExpense;
import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.User;
import com.adt.payroll.model.payload.PriorTimeManagementRequest;
import com.adt.payroll.msg.ResponseModel;
import com.adt.payroll.repository.EmployeeExpenseRepo;
import com.adt.payroll.repository.EmployeeRepo;
import com.adt.payroll.repository.PriorTimeRepository;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.repository.UserRepo;

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
	private EmployeeExpenseRepo employeeExpenseRepo;

	@Value("${Expenses_Invoice_Path}")
	private String invoicePath;

	@Autowired
	private Util util;

	@Value("${latitude}")
	private double COMPANY_LATITUDE;

	@Value("${longitude}")
	private double COMPANY_LONGITUDE;

	private static final double MAX_DISTANCE_THRESHOLD = 100.0;

	@Override
	public String updateCheckIn(int empId, double latitude, double longitude) {
		double distance = calculateDistance(latitude, longitude, COMPANY_LATITUDE, COMPANY_LONGITUDE);

		if (distance <= MAX_DISTANCE_THRESHOLD) {
			CurrentDateTime currentDateTime = util.getDateTime();
			Optional<TimeSheetModel> lista = timeSheetRepo.findByEmployeeIdAndDate(empId,
					currentDateTime.getCurrentDate());
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
			} else {
				TimeSheetModel timeSheetModel = lista.get();
				if (timeSheetModel.getCheckOut() != null) {
					return "You Are Already Check Out For The Day";
				}
				return "You Are Already Check in";
			}
		} else {
			// Employee is outside the company, return error response
			return "Check-in not allowed. Employee is outside the company.";
		}
	}

	@Override
	public String updateCheckOut(int empId, double latitude, double longitude) throws ParseException {
		double distance = calculateDistance(latitude, longitude, COMPANY_LATITUDE, COMPANY_LONGITUDE);

		if (distance <= MAX_DISTANCE_THRESHOLD) {
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
				timeSheetRepo.save(timeSheetModel);
				return "Check_Out Successfully";
			}
			return "You Are Not Check in";
		} else {
			// Employee is outside the company, return error response
			return "Check-in not allowed. Employee is outside the company.";
		}
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

	//*** JIRA:- HRMS-91 ***
	public Optional<Priortime> savePriorTime(PriorTimeManagementRequest priorTimeManagementRequest)
			throws ParseException {
		Priortime priortimeuser = new Priortime();

		if (priorTimeManagementRequest.getCheckIn() != null && !priorTimeManagementRequest.getCheckIn().equals("")) {
			priortimeuser.setCheckIn(priorTimeManagementRequest.getCheckIn());
		} else {
			Optional<TimeSheetModel> timeSheetModelData = timeSheetRepo.findByEmployeeIdAndDate(
					priorTimeManagementRequest.getEmployeeId(), priorTimeManagementRequest.getDate());

			if(timeSheetModelData.isPresent())
				priortimeuser.setCheckIn(timeSheetModelData.get().getCheckIn());
			else
				return null;
		}
		if (priorTimeManagementRequest.getCheckOut() != null && !priorTimeManagementRequest.getCheckOut().equals("")) {
			priortimeuser.setCheckOut(priorTimeManagementRequest.getCheckOut());
		} else {

			String checkout = timeSheetRepo.findCheckOutByEmployeeIdAndDate(priorTimeManagementRequest.getEmployeeId(),
					priorTimeManagementRequest.getDate());

			priortimeuser.setCheckOut(checkout);
		}
		priortimeuser.setDate(priorTimeManagementRequest.getDate());

		//*** Get the User info from the DB ***
		Optional<User> userData = userRepo.findById(priorTimeManagementRequest.getEmployeeId());

		if(userData.isPresent()) {

			priortimeuser.setEmail(userData.get().getEmail());
			priortimeuser.setEmployeeId(userData.get().getId());

			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
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
		return null;
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

	//	@Override
	//	public List<TimesheetDTO> empAttendence(int empId, LocalDate fromDate, LocalDate toDate) {
	//		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	//		String startDate = String.valueOf(dateTimeFormatter.format(fromDate));
	//		String endDate = String.valueOf(dateTimeFormatter.format(toDate));
	//		List<TimeSheetModel> timeSheetModelList = timeSheetRepo.findAllByEmployeeId(empId, startDate, endDate);
	//		if (timeSheetModelList.isEmpty()) {
	//			throw new NullPointerException("No attendence data available with given ID: " + empId);
	//		}
	//		List<TimesheetDTO> timesheetDTOList = new ArrayList<TimesheetDTO>();
	//		for (TimeSheetModel timeSheetModel : timeSheetModelList) {
	//			TimesheetDTO timesheetDTO = TimesheetDTO.builder().employeeId(timeSheetModel.getEmployeeId())
	//					.date(timeSheetModel.getDate()).checkIn(timeSheetModel.getCheckIn())
	//					.checkOut(timeSheetModel.getCheckOut()).workingHour(timeSheetModel.getWorkingHour())
	//					.leaveInterval(timeSheetModel.getLeaveInterval()).status(timeSheetModel.getStatus()).build();
	//			timesheetDTOList.add(timesheetDTO);
	//		}
	//		return timesheetDTOList;
	//	}

	// JIRA no. - HRMS-88
	@Override
	public List<TimesheetDTO> empAttendence(int empId, String fromDate, String toDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy"); 
		LocalDate startDate = LocalDate.parse(fromDate, formatter);
		LocalDate endDate = LocalDate.parse(toDate, formatter);
		List<TimeSheetModel> list = timeSheetRepo.findAllByEmployeeId(empId);
		List<TimeSheetModel> list2= list.stream().filter(t->{
			LocalDate date = LocalDate.parse(t.getDate(), formatter);
			int d1=date.compareTo(startDate);
			int d2=date.compareTo(endDate);
			if((d1>0 && d2<0) || d1==0 || 0==d2) {
				return true;
			}
			return false;
		}).collect(Collectors.toList());
		List<TimesheetDTO> timesheetDTOList = new ArrayList<TimesheetDTO>();
		for (TimeSheetModel timeSheetModel : list2) {
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
		String startDate = String.valueOf(dateTimeFormatter.format(fromDate));
		String endDate = String.valueOf(dateTimeFormatter.format(toDate));
		List<TimeSheetModel> list = timeSheetRepo.findAllByEmployeeId(startDate, endDate).stream()
				.filter(e -> !e.equals(null)).map(e -> {
					Optional<User> t = userRepo.findById(e.getEmployeeId());
					if (t.isPresent()) {
						e.setEmployeeName(t.get().getFirstName() + " " + t.get().getLastName());
					} else {
						e.setEmployeeName("NOT AVAILABLE");
					}
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

	//	    public static EmployeeExpense saveEmployeeExpenseDetail(EmployeeExpense employeeExpense, List<MultipartFile> image) throws IOException, SQLException {
	//	        Blob blob = new SerialBlob(IOUtils.toByteArray(image.getInputStream()));
	//	        employeeExpense.setImage(blob);
	//	        return employeeExpense;
	//	    }

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

	//	    @Override
	//	    public EmployeeExpenseDTO rejectedEmployeeExpense(int expenseId) {
	//	        Optional<EmployeeExpense> employeeExpenseOptional = employeeExpenseRepo.findById(expenseId);
	//	        EmployeeExpenseDTO employeeExpenseDTO = new EmployeeExpenseDTO();
	//	        if (employeeExpenseOptional.isPresent()) {
	//	            EmployeeExpense employeeExpense = employeeExpenseOptional.get();
	//	            employeeExpense.setStatus("Rejected");
	//	            EmployeeExpense employeeExpense1 = employeeExpenseRepo.save(employeeExpense);
	//	            String employeeId = employeeExpense1.getEmployeeId();
	//	            Optional<Employee> employeeOptional = employeeRepo.findByEmpId(Integer.parseInt(employeeId));
	//	            if (employeeOptional.isPresent()) {
	//	                Employee employee = employeeOptional.get();
	//	                employeeExpenseDTO.setStatus(employeeExpense1.getStatus());
	//	                changeToDTO(employeeExpense1, employeeExpenseDTO, employee);
	//	                if (employeeExpense1.getInvoices() != null) {
	//	                    String invoices = employeeExpense1.getInvoices();
	//	                    HashMap<String, File> map = new HashMap<>();
	//	                    if (invoices.contains(",")) {
	//	                        String[] filePaths = invoices.split(",");
	//	                        for (String filePath : filePaths) {
	//	                            File file = getFileFromPath(filePath);
	//	                            map.put(file.getName(), file);
	//	                        }
	//	                    } else {
	//	                        File file = getFileFromPath(invoices);
	//	                        map.put(file.getName(), file);
	//	                    }
	//	                    employeeExpenseDTO.setInvoices(map);
	//	                }
	//	            }
	//	            return employeeExpenseDTO;
	//	        } else {
	//	            String message = "Expense id: " + expenseId + " does not exists";
	//	            logger.error(message);
	//	            throw new RuntimeException(message);
	//	        }
	//	    }

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
	public EmployeeExpenseDTO getEmployeeExpenseById(int expenseId) {
		Optional<EmployeeExpense> employeeExpenseOptional = employeeExpenseRepo.findById(expenseId);
		EmployeeExpenseDTO employeeExpenseDTO = new EmployeeExpenseDTO();
		if (employeeExpenseOptional.isPresent()) {
			EmployeeExpense employeeExpense = employeeExpenseOptional.get();
			String employeeId = employeeExpense.getEmployeeId();
			Optional<User> employeeOptional = userRepo.findById(Integer.parseInt(employeeId));
			if (employeeOptional.isPresent()) {
				User employee = employeeOptional.get();
				// employeeExpenseDTO.setStatus(employeeExpense.getStatus());
				changeToDTO(employeeExpense, employeeExpenseDTO, employee);
				if (employeeExpense.getInvoices() != null) {
					String invoices = employeeExpense.getInvoices();
					HashMap<String, File> map = new HashMap<>();
					if (invoices.contains(",")) {
						String[] filePaths = invoices.split(",");
						for (String filePath : filePaths) {
							File file = getFileFromPath(filePath);
							map.put(file.getName(), file);
						}
					} else {
						File file = getFileFromPath(invoices);
						map.put(file.getName(), file);
					}
					employeeExpenseDTO.setInvoices(map);
				}

			}
			return employeeExpenseDTO;
		} else {
			String message = "Expense id: " + expenseId + " does not exists";
			LOGGER.error(message);
			throw new RuntimeException(message);
		}
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

	/*
	 * @Override public EmployeeExpenseDTO employeeExpense(int empId,
	 * List<MultipartFile> image, EmployeeExpense employeeExpense) throws
	 * IOException, SQLException { // TODO Auto-generated method stub return null; }
	 */
}
