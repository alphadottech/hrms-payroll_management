package com.adt.payroll.scheduler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import com.adt.payroll.model.LeaveBalance;
import com.adt.payroll.repository.LeaveBalanceRepository;
//import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.adt.payroll.exception.NoDataFoundException;
import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.LeaveRepository;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.repository.UserRepo;
import com.adt.payroll.service.CommonEmailService;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MonthlyScheduler {

	private static final Logger log = LogManager.getLogger(MonthlyScheduler.class);

	@Autowired
	private LeaveRepository leaveRepository;

	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private CommonEmailService mailService;
	@Autowired
	private LeaveBalanceRepository leaveBalanceRepo;

	public void updateAbsentDaysInDatabase() {
		timeSheetRepo.updateAbsentDays();
	}

	@Value("${holiday}")
	private String[] holiday;

	@Scheduled(cron = "0 0 0 1 * *") // Executes on the 1st day of each month at midnight
	public void updateColumnValue() {
		List<LeaveModel> leaveModelList = leaveRepository.findAll();
		for (LeaveModel lm : leaveModelList) {
			lm.setLeaveBalance(lm.getLeaveBalance() + 1);
			leaveRepository.save(lm);
		}

	}

	//@Scheduled(cron = "0 */2 * * * *")
	@Scheduled(cron = "0 0 8 * * MON") // Executes on the every Monday at 8 AM
	public void sendNotificationForTimeSheet() {
		log.info("Generate weekly time sheet report ");
		LocalDate currentDate = LocalDate.now();
		LocalDate endDate = currentDate.minusDays(1);
		LocalDate startDate = currentDate.minusDays(7);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		List<TimeSheetModel> timeSheet = timeSheetRepo.findTimeSheetWithNullValues(startDate.format(formatter),
				endDate.format(formatter));
		LocalDateTime fixedWorkingHour = LocalDateTime.now().withHour(9).withMinute(30).withSecond(0).withNano(0);

		if (!timeSheet.isEmpty() && timeSheet.size() > 0) {
			Map<Integer, List<TimeSheetModel>> employeeTimeSheetDetails = timeSheet.stream().filter(ts -> {
				LocalDateTime empWorkingHours = null;
				if (ts.getWorkingHour() != null && !ts.getWorkingHour().isEmpty()) {
					String[] arrayOfHours = ts.getWorkingHour().split(":");
					int hour = Integer.parseInt(arrayOfHours[0]);
					int minute = Integer.parseInt(arrayOfHours[1]);
					empWorkingHours = LocalDateTime.now().withHour(hour).withMinute(minute).withSecond(0).withNano(0);
					boolean isBefore = empWorkingHours.isBefore(fixedWorkingHour);
					return isBefore;
				} else {
					return true;
				}

			}).collect(Collectors.groupingBy(TimeSheetModel::getEmployeeId));

			employeeTimeSheetDetails.forEach((i, e) -> {
				try {
					Optional<User> user = Optional.ofNullable(userRepo.findById(i)
							.orElseThrow(() -> new NoDataFoundException("employee not found :" + i)));
					ByteArrayOutputStream employeeReport = generateExcelReport(e);
					mailService.sendEmailForTimeSheet(employeeReport,
							user.get().getFirstName() + " " + user.get().getLastName(), user.get().getEmail(),
							startDate.format(formatter) + " to " + endDate.format(formatter));

				} catch (IOException ex) {
					log.error("Error while generating timesheet report.", ex.getMessage());
				}
			});
		}
	}

	private ByteArrayOutputStream generateExcelReport(List<TimeSheetModel> timeSheet) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("TimeSheet Report");

		Row headerRow = sheet.createRow(0);
		String[] headers = {"ID", "Check-In", "Check-Out", "Working Hour", "Date", "Day"};
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
		}
		int rowNum = 1;
		for (TimeSheetModel timeSheetModel : timeSheet) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(String.valueOf(timeSheetModel.getEmployeeId()));
			row.createCell(1).setCellValue(timeSheetModel.getCheckIn() != null ? timeSheetModel.getCheckIn() : "NULL");
			row.createCell(2)
					.setCellValue(timeSheetModel.getCheckOut() != null ? timeSheetModel.getCheckOut() : "NULL");
			row.createCell(3)
					.setCellValue(timeSheetModel.getWorkingHour() != null ? timeSheetModel.getWorkingHour() : "NULL");
			row.createCell(4).setCellValue(timeSheetModel.getDate() != null ? timeSheetModel.getDate() : "NULL");
			row.createCell(5).setCellValue(timeSheetModel.getDay() != null ? timeSheetModel.getDay() : "NULL");
		}

		try (ByteArrayOutputStream fileOut = new ByteArrayOutputStream()) {
			workbook.write(fileOut);
			workbook.close();
			return fileOut;
		}

	}

	//@Scheduled(cron = "0 */1 * * * *")
 @Scheduled(cron = "0 0 8 * * MON") // Executes every Monday at 8 AM
	public String sendLeaveNotificationForTimesheet() {
		log.info("Generate time sheet report and handle leaves for Date");
		LocalDate currentDate = LocalDate.now();
		LocalDate startDate = currentDate.withDayOfMonth(1);
		LocalDate endDate = currentDate.minusDays(1); // Set endDate to yesterday
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String formattedStartDate = startDate.format(formatter);
		String formattedEndDate = endDate.format(formatter);
		List<TimeSheetModel> timeSheet = timeSheetRepo.findTimeSheetWithNullValues(formattedStartDate, formattedEndDate);

		// Get working dates for the current month up to and including yesterday
		List<LocalDate> workingDates = calculateWorkingDates(startDate, endDate);

		if (!timeSheet.isEmpty()) {
			Map<Integer, List<TimeSheetModel>> employeeTimeSheetDetails = timeSheet.stream()
					.collect(Collectors.groupingBy(TimeSheetModel::getEmployeeId));

			for (Map.Entry<Integer, List<TimeSheetModel>> entry : employeeTimeSheetDetails.entrySet()) {
				Integer employeeId = entry.getKey();
				List<TimeSheetModel> timeSheets = entry.getValue();

				try {
					// Mark absences and log absent dates
					List<LocalDate> absentDates = markAbsences(timeSheets, workingDates, employeeId);
					if (absentDates.isEmpty()) {
						log.info("No absences recorded for employee: {}", employeeId);
					} else {
						log.info("Absence recorded successfully for employee: {}. Absent Dates: {}", employeeId, absentDates);
					}
				} catch (Exception ex) {
					log.error("Error processing leave notification for employeeId: {}. Exception message: {}", employeeId, ex.getMessage(), ex);
				}
			}
			return "Leave notifications sent successfully for timesheets.";
		} else {
			log.info("No timesheets found for the current month.");
			return "No timesheets found for the current month.";
		}
	}

	private List<LocalDate> markAbsences(List<TimeSheetModel> timeSheets, List<LocalDate> workingDates, int employeeId) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		Set<LocalDate> datesWithRecords = timeSheets.stream()
				.map(ts -> {
					LocalDate date = LocalDate.parse(ts.getDate(), formatter);
					log.info("Recorded date for employee {}: {}", employeeId, date);
					return date;
				})
				.collect(Collectors.toSet());

		List<LocalDate> absentDates = new ArrayList<>();

		for (LocalDate date : workingDates) {
			log.info("Checking working date: {}", date);
			if (!datesWithRecords.contains(date)) {
				absentDates.add(date);
				try {
					// Mark absence in the database
					TimeSheetModel absenceRecord = new TimeSheetModel();
					absenceRecord.setEmployeeId(employeeId);
					absenceRecord.setDate(date.format(formatter));
					absenceRecord.setStatus("Absent");
					absenceRecord.setDay(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)); // Set day of the week

					// Save or update the record in the database
					TimeSheetModel existingRecord = timeSheetRepo.findByEmployeeIdAndDate(employeeId, date.format(formatter)).orElse(null);
					if (existingRecord != null) {
						// Update the existing record
						existingRecord.setStatus("Absent");
						existingRecord.setDay(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
						timeSheetRepo.save(existingRecord);
					} else {
						// Save a new record
						timeSheetRepo.save(absenceRecord);
					}

					log.info("Employee ID {} was absent on {} ({})", employeeId, date, absenceRecord.getDay());
				} catch (Exception e) {
					log.error("Error marking absence for employeeId: {} on date: {}", employeeId, date, e);
				}
			}
		}

		return absentDates;
	}

	private List<LocalDate> calculateWorkingDates(LocalDate startDate, LocalDate endDate) {
		List<LocalDate> workingDates = new ArrayList<>();
		LocalDate currentDate = startDate;

		while (!currentDate.isAfter(endDate)) {
			workingDates.add(currentDate);
			currentDate = currentDate.plusDays(1);
		}

		return workingDates;
	}






//	@Scheduled(cron = "0 */1 * * * *")
	//@Scheduled(cron = "0 0 0 28-31 * ?") // Executes at midnight on the 28th to 31st of every month
//	@Transactional
//	public String sendLeaveNotificationOnMonthlyBasis() {
//		LocalDate currentDate = LocalDate.now();
//		LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
//		// This block ensures the task runs only on the last day of the month
//		if (!currentDate.isEqual(lastDayOfMonth)) {
//			return "Not the last day of the month, task not executed.";
//		}
//		// Fetch all users
//		List<User> users = userRepo.findAll();
//		users.forEach(user -> {
//			try {
//				int employeeId = user.getId();
//				Optional<LeaveBalance> leaveBalanceOpt = Optional.ofNullable(leaveBalanceRepo.findByEmpId(employeeId));
//				LeaveBalance leaveBalance = leaveBalanceOpt.orElseGet(() -> {
//					LeaveBalance newLeaveBalance = new LeaveBalance();
//					newLeaveBalance.setEmpId(employeeId);
//					newLeaveBalance.setLeaveBalance(1); // Initialize paid leave to 0
//					return newLeaveBalance;
//				});
//				// Allocate 1 paid leave at the start of the month
//				leaveBalance.setLeaveBalance(leaveBalance.getPaidLeave() + 1);
//				// Save the updated leave balance
//				leaveBalanceRepo.save(leaveBalance);
//				log.info("Leave notification and balance updated successfully for employee: {} {}", user.getFirstName(), user.getLastName());
//			} catch (Exception ex) {
//				log.error("Error processing leave notification for employeeId: {}. Exception message: {}", user.getId(), ex.getMessage(), ex);
//			}
//		});
//		return "Leave notifications and balances sent successfully for users.";
//	}
}
