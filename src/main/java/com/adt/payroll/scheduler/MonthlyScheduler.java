package com.adt.payroll.scheduler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.adt.payroll.service.PayRollServiceImpl;

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

    @Scheduled(cron = "0 0 0 1 * *") // Executes on the 1st day of each month at midnight
    public void updateColumnValue() {
           List<LeaveModel> leaveModelList = leaveRepository.findAll();
           for(LeaveModel lm : leaveModelList){
               lm.setLeaveBalance(lm.getLeaveBalance()+1);
               leaveRepository.save(lm);
           }

    }
  
   // @Scheduled(cron = "0 */2 * * * *")
    @Scheduled(cron = "0 0 8 * * MON") // Executes on the every Monday at 8 AM
	public void sendNotificationForTimeSheet() {
		log.info("Generate weekly time sheet report ");
		LocalDate currentDate = LocalDate.now();
		LocalDate endDate = currentDate.minusDays(1);
		LocalDate startDate = currentDate.minusDays(7);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		List<TimeSheetModel> timeSheet = timeSheetRepo.findTimeSheetWithNullValues(startDate.format(formatter),
				endDate.format(formatter));
		if (!timeSheet.isEmpty() && timeSheet.size() > 0) {
			Map<Integer, List<TimeSheetModel>> employeeTimeSheetDetails = timeSheet.stream()
					.collect(Collectors.groupingBy(TimeSheetModel::getEmployeeId));
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
		String[] headers = { "ID", "Check-In", "Check-Out", "Working Hour", "Date" };
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
		}

		try (ByteArrayOutputStream fileOut = new ByteArrayOutputStream()) {
			workbook.write(fileOut);
			workbook.close();
			return fileOut;
		}

	}
}
