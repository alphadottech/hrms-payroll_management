package com.adt.payroll.service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.adt.payroll.dto.SalaryDTO;
import com.adt.payroll.dto.SalaryDetailsDTO;
import com.adt.payroll.dto.ViewPaySlipDto;
import com.adt.payroll.exception.NoDataFoundException;
import com.adt.payroll.model.EmpPayrollDetails;
import com.adt.payroll.model.Employee;
import com.adt.payroll.model.ExpenseItems;
import com.adt.payroll.model.ImageModel;
import com.adt.payroll.model.LeaveBalance;
import com.adt.payroll.model.MonthlySalaryDetails;
import com.adt.payroll.model.PaySlip;
import com.adt.payroll.model.SalaryDetails;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.EmpPayrollDetailsRepo;
import com.adt.payroll.repository.ExpenseManagementRepo;
import com.adt.payroll.repository.ImageRepo;
import com.adt.payroll.repository.LeaveBalanceRepository;
import com.adt.payroll.repository.MonthlySalaryDetailsRepo;
import com.adt.payroll.repository.PayRecordRepo;
import com.adt.payroll.repository.SalaryDetailsRepository;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.repository.UserRepo;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.text.DocumentException;

@Service
public class PayRollServiceImpl implements PayRollService {

	private static final Logger log = LogManager.getLogger(PayRollServiceImpl.class);

	@Autowired
	private JavaMailSender javaMailSender;

//    @Value("${spring.mail.username}")
//    private String sender;

	@Autowired
	private MonthlySalaryDetailsRepo monthlySalaryDetailsRepo;

	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Autowired
	private PayRecordRepo payRecordRepo;
	@Autowired
	private TableDataExtractor dataExtractor;

	@Autowired
	private UserRepo userRepo;
	@Autowired
	private SalaryDetailsRepository salaryDetailsRepo;

	@Autowired
	EmpPayrollDetailsRepo empPayrollDetailsRepo;

	@Autowired
	private ImageRepo imgRepo;
	@Autowired
	private LeaveBalanceRepository leaveBalanceRepo;
	@Autowired
	ExpenseManagementRepo expenseManagementRepo;

	@Value("${holiday}")
	private String[] holiday;

	@Autowired
	Util util;

	public String invalidValue = "";

	public Integer allFieldeValue;
	
	public String adtID="";

	@Autowired
	private CommonEmailService mailService;

	public PaySlip createPaySlip(int empId, String month, String year) throws ParseException, IOException {
		log.info("inside method");
		String submitDate = "", status = "", employee_id = "";
		String monthYear = month + " " + year;
		int yourWorkingDays = 0, leaves = 0, workDays = 0, saturday = Util.SaturdyaValue, adhoc = 0;
		LocalDate currentdate = LocalDate.now();
		PaySlip paySlip = new PaySlip();
		String sql = "select * from employee_schema.employee_expenses";
		List<Map<String, Object>> tableData = dataExtractor.extractDataFromTable(sql);

		List<String> holidays = Arrays.asList(holiday);
		List<String> lists = new ArrayList<>();

		SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
		SimpleDateFormat outputFormat = new SimpleDateFormat("MM"); // 01-12

		Calendar cal = Calendar.getInstance();
		cal.setTime(inputFormat.parse(month));

		Optional<EmpPayrollDetails> empDetails = Optional.ofNullable(empPayrollDetailsRepo.findById(empId)
				.orElseThrow(() -> new NoDataFoundException("employee not found :" + empId)));

		Optional<User> user = Optional.ofNullable(
				userRepo.findById(empId).orElseThrow(() -> new NoDataFoundException("employee not found :" + empId)));
		String name = user.get().getFirstName() + " " + user.get().getLastName();
		List<TimeSheetModel> timeSheetModel = timeSheetRepo.search(empId, month.toUpperCase(), year);

		yourWorkingDays = timeSheetModel.stream()
				.filter(x -> x.getWorkingHour() != null && x.getStatus().equalsIgnoreCase(Util.StatusPresent))
				.collect(Collectors.toList()).size();
		leaves = timeSheetModel.stream().filter(
				x -> x.getWorkingHour() == null && (x.getCheckIn() == null && x.getStatus().equalsIgnoreCase("Leave")))
				.collect(Collectors.toList()).size();

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String monthDate = String.valueOf(outputFormat.format(cal.getTime()));

		String firstDayMonth = "01/" + monthDate + "/" + year;
		String lastDayOfMonth = (LocalDate.parse(firstDayMonth, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
				.with(TemporalAdjusters.lastDayOfMonth())).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date startDate = formatter.parse(firstDayMonth);
		Date endDate = formatter.parse(lastDayOfMonth);

		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		Calendar end = Calendar.getInstance();
		end.setTime(endDate);

		LocalDate localDate = null;
		while (!start.after(end)) {
			localDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (start.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
				lists.add(localDate.toString());

			start.add(Calendar.DATE, 1);
		}

		lists.removeAll(holidays);
		workDays = lists.size();

		for (Map<String, Object> expense : tableData) {
			String paymentDate = String.valueOf(expense.get("payment_date"));
			paymentDate = paymentDate != null ? paymentDate.trim() : "";
			submitDate = paymentDate.length() >= 5 ? paymentDate.substring(3, 5) : "";
			status = String.valueOf(expense.get("status"));
			employee_id = String.valueOf(expense.get("employee_id"));
			if (submitDate.equals(monthDate) && status.equals("Accepted")
					&& employee_id.equalsIgnoreCase(String.valueOf(empId))) {
				adhoc += Integer.parseInt(String.valueOf(expense.get("expense_amount")));
			}
		}

		float grossSalary = 0.0f;
		if (empDetails.get().getSalary() != null) {
			grossSalary = empDetails.get().getSalary().floatValue();
		}
		int totalWorkingDays = workDays - saturday;
		float amountPerDay = grossSalary / totalWorkingDays;
		float leavePerDay = leaves * amountPerDay;
		float netAmount = (yourWorkingDays * amountPerDay);
		netAmount += adhoc;
		paySlip = new PaySlip(empId, name, empDetails.get().getDesignation(), dtf.format(currentdate),
				empDetails.get().getBankName(), empDetails.get().getAccountNumber(),
				firstDayMonth + " - " + lastDayOfMonth, yourWorkingDays, totalWorkingDays, leaves, leavePerDay,
				grossSalary, netAmount, adhoc);
		ImageModel img = new ImageModel();
		ImageData datas = null;
		if (imgRepo.search() != null) {
			datas = ImageDataFactory.create(imgRepo.search());
		} else {
			datas = Util.getImage();
		}

		log.info("image path set");
		Image alpha = new Image(datas);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter pdfWriter = new PdfWriter(baos);
		PdfDocument pdfDocument = new PdfDocument(pdfWriter);
		Document document = new Document(pdfDocument);

		pdfDocument.setDefaultPageSize(PageSize.A4);
		float col = 250f;
		float columnWidth[] = { col, col };
		Table table = new Table(columnWidth);
		table.setBackgroundColor(new DeviceRgb(63, 169, 219)).setFontColor(Color.WHITE);
		table.addCell(new Cell().add("Pay Slip").setTextAlignment(TextAlignment.CENTER)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setMarginTop(30f).setMarginBottom(30f).setFontSize(30f)
				.setBorder(Border.NO_BORDER));
		table.addCell(new Cell().add(Util.ADDRESS).setTextAlignment(TextAlignment.RIGHT).setMarginTop(30f)
				.setMarginBottom(30f).setBorder(Border.NO_BORDER).setMarginRight(10f));
		float colWidth[] = { 150, 150, 100, 100 };
		Table employeeTable = new Table(colWidth);
		employeeTable.addCell(new Cell(0, 4).add(Util.EmployeeInformation).setBold());
		employeeTable.addCell(new Cell().add(Util.EmployeeNumber).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(String.valueOf(user.get().getId())).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.Date).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(dtf.format(currentdate)).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.Name).setBorder(Border.NO_BORDER));
		employeeTable.addCell(
				new Cell().add(user.get().getFirstName() + " " + user.get().getLastName()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.BankName).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(empDetails.get().getBankName()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.JobTitle).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(empDetails.get().getDesignation()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.AccountNumber).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(empDetails.get().getAccountNumber()).setBorder(Border.NO_BORDER));

		Table itemInfo = new Table(columnWidth);
		itemInfo.addCell(new Cell().add(Util.PayPeriods));
		itemInfo.addCell(new Cell().add(firstDayMonth + " - " + lastDayOfMonth));
		itemInfo.addCell(new Cell().add(Util.YourWorkingDays));
		itemInfo.addCell(new Cell().add(String.valueOf(yourWorkingDays)));
		itemInfo.addCell(new Cell().add(Util.TotalWorkingDays));
		itemInfo.addCell(new Cell().add(String.valueOf(totalWorkingDays)));
		itemInfo.addCell(new Cell().add("Adhoc Amount"));
		itemInfo.addCell(new Cell().add(String.valueOf(adhoc)));
		itemInfo.addCell(new Cell().add(Util.NumberOfLeavesTaken));
		itemInfo.addCell(new Cell().add(String.valueOf(leaves)));
		itemInfo.addCell(new Cell().add(Util.GrossSalary));
		itemInfo.addCell(new Cell().add(String.valueOf(grossSalary)));
		itemInfo.addCell(new Cell().add(Util.NetAmountPayable));
		itemInfo.addCell(new Cell().add(String.valueOf(netAmount)));
		document.add(alpha);

		document.add(table);
		document.add(new Paragraph("\n"));
		document.add(employeeTable);
		document.add(itemInfo);
		document.add(
				new Paragraph("\n(Note - This is a computer generated statement and does not require a signature.)")
						.setTextAlignment(TextAlignment.CENTER));
		document.close();
		log.warn("Successfully");

		// sendEmail(baos, name, user.get().getEmail(), monthYear);

		mailService.sendEmail(baos, name, user.get().getEmail(), monthYear);

		return paySlip;
	}

	// Excel Pay Slip

	public String generatePaySlip(MultipartFile file, String email) throws IOException, ParseException {
		DateTimeZone istTimeZone = DateTimeZone.forID("Asia/Kolkata");
		DateTime currentDateTime = new DateTime(istTimeZone);

		Timestamp lastUpdatedDate = monthlySalaryDetailsRepo.findLatestSalaryUpdatedDate();

		if (lastUpdatedDate != null) {
			DateTime lastUpdatedDateTime = new DateTime(lastUpdatedDate.getTime(), istTimeZone);

			Duration duration = new Duration(lastUpdatedDateTime, currentDateTime);
			long minutes = duration.getStandardMinutes();
			if (minutes <= 10) {
				return "You have generated the payslip " + minutes + " mins ago. Please try again after 10 mins.";
			}
		}

		String empId = "", name = "", salary = "", esic = "", pf = "", paidLeave = "", bankName = "",
				accountNumber = "", gmail = "", designation = "", submitDate = "", status = "", employee_id = "",
				joiningDate = "";
		String sheetName = "";
		int adjustment = 0, tds = 0, adhoc1 = 0, medicalInsurance = 0, adhoc3 = 0, workingDays = 0, present = 0,
				leave = 0, halfDay = 0, limit = 30;
		Map<String, Integer> excelColumnName = new HashMap<>();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
		SimpleDateFormat outputFormat = new SimpleDateFormat("MM");
		NumberFormat format = NumberFormat.getInstance();
		String projDir = System.getProperty("user.dir");
		XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
		DataFormatter dataFormatter = new DataFormatter();

		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			XSSFSheet sh = workbook.getSheetAt(i);
			if (sh.getLastRowNum() > 0) {
				sheetName = sh.getSheetName();
			}
		}

		XSSFSheet sheet = workbook.getSheet(sheetName);

		Row headerRow = sheet.getRow(0);

		int columnCount = headerRow.getLastCellNum();
		String columnHeader = "";
		for (int i = 0; i < columnCount; i++) {
			headerRow.getCell(i);
			headerRow.getCell(i).getStringCellValue();
			columnHeader = String.valueOf(headerRow.getCell(i)).trim();

			excelColumnName.put(columnHeader, i);
		}

		LocalDate currentdate = LocalDate.now();
		LocalDate earlier = currentdate.minusMonths(1);

		Calendar cal = Calendar.getInstance();
		cal.setTime(inputFormat.parse(String.valueOf(earlier.getMonth())));

		String monthDate = String.valueOf(outputFormat.format(cal.getTime()));
//
//		String monthYear = String.valueOf(earlier.getMonth() + " " + earlier.getYear());
//		String firstDayMonth = "01/" + monthDate + "/" + +earlier.getYear();
//		String lastDayOfMonth = (LocalDate.parse(firstDayMonth, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
//				.with(TemporalAdjusters.lastDayOfMonth())).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
//		String payPeriod = firstDayMonth + " - " + lastDayOfMonth;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String date = dtf.format(currentdate);
		String sql = "select * from employee_schema.employee_expenses";
		List<Map<String, Object>> tableData = dataExtractor.extractDataFromTable(sql);
		List<User> employee = userRepo.findAll();
		Map<String, String> paySlipDetails = util.getWorkingDaysAndMonth();
		// int workingDay = util.getWorkingDays();
		for (int i = 2; i <= sheet.getLastRowNum(); i++) {

			try {
				XSSFRow row = sheet.getRow(i);
				try {

					if (isNotNull(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.EmployeeNumber))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Name))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.TotalWorkingDays))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.YourWorkingDays))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Leave))),
							(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.HalfDay)))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.salary))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.PaidLeave))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.BankName))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.AccountNumber))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.DESIGNATION))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Gmail))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.JoiningDate))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Esic))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.PF))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.ADJUSTMENT))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.TDS))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.MEDICAL_INSURANCE))),
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc1))),
							paySlipDetails)) {
						if (allFieldeValue < 19) {
							mailService.sendEmail(
									dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Name))),
									invalidValue);
							continue;
						} else {
							continue;
						}

					}

					empId = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.EmployeeNumber)));
					name = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Name)));
					workingDays = Integer.parseInt(
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.TotalWorkingDays))));
					present = Integer.parseInt(
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.YourWorkingDays))));
					leave = Integer
							.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Leave))));
					halfDay = Integer
							.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.HalfDay))));
					salary = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.salary)));
					paidLeave = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.PaidLeave)));
					bankName = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.BankName)));
					accountNumber = format
							.format(row.getCell(excelColumnName.get(Util.AccountNumber)).getNumericCellValue())
							.replace(",", "");
					designation = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.DESIGNATION)));
					gmail = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Gmail)));
					joiningDate = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.JoiningDate)));
					esic = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Esic)));
					pf = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.PF)));

					try {
						adjustment = Integer.parseInt(
								dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.ADJUSTMENT))));
					} catch (NumberFormatException e) {
						adjustment = 0;
					}
					try {
						tds = Integer
								.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.TDS))));
					} catch (NumberFormatException e) {
						tds = 0;
					}
					try {
						medicalInsurance = Integer.parseInt(dataFormatter
								.formatCellValue(row.getCell(excelColumnName.get(Util.MEDICAL_INSURANCE))));
					} catch (NumberFormatException e) {
						medicalInsurance = 0;
					}

					try {
						adhoc1 = Integer
								.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc1))));
					} catch (NumberFormatException e) {
						adhoc1 = 0;
					}
					String[] fullName = name.split(" ");
					String fName = fullName[0].toString();
					String lName = fullName[1].toString();
					if (halfDay > limit || leave > limit || workingDays > limit || present > limit) {
						continue;
					}
					for (Map<String, Object> expense : tableData) {
						submitDate = String.valueOf(expense.get("payment_date")).substring(3, 5);
						status = String.valueOf(expense.get("status"));
						employee_id = String.valueOf(expense.get("employee_id"));
						if (submitDate.equals(monthDate) && status.equals("Accepted")
								&& employee_id.equalsIgnoreCase(String.valueOf(empId))) {
							adhoc1 += Integer.parseInt(String.valueOf(expense.get("expense_amount")));
						}
					}

					MonthlySalaryDetails monthlySalaryDetails = new MonthlySalaryDetails();

					if (checkEmpDetails(empId, gmail, accountNumber, employee, fName, lName)) {
						log.error("Getting error while validating the field=" + invalidValue);
						mailService.sendEmail(name, invalidValue);
						continue;

					}
					log.info("Generating Pdf");
					baos = createPdf(adtID, name, workingDays, present, leave, halfDay, salary, paidLeave, date,
							bankName, accountNumber, designation, joiningDate, adhoc1,
							paySlipDetails.get(Util.PAY_PERIOD), esic, pf, adjustment, medicalInsurance, tds,
							monthlySalaryDetails);

					log.info("Pdf generated successfully.");

					SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
					Calendar cal1 = Calendar.getInstance();
					cal1.add(Calendar.MONTH, -1);
					SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
					String monthName = monthFormat.format(cal1.getTime()).toUpperCase();
					DateTime current = new DateTime(istTimeZone);
					double medical = medicalInsurance;
					double adhoc = adhoc1;
					double adj = adjustment;

					if (email == null || email.isEmpty()) {
						log.info("save data in monthlySalaryDetails Table");
						monthlySalaryDetails.setEmpId(Integer.parseInt(empId));
						monthlySalaryDetails.setMedicalInsurance(medical);
						monthlySalaryDetails.setAdhoc(adhoc);
						monthlySalaryDetails.setAdjustment(adj);
						monthlySalaryDetails.setDearnessAllowance(0.0);
						monthlySalaryDetails.setCreditedDate(util.getCreatedDate(15));
						monthlySalaryDetails.setMonth(monthName);
						monthlySalaryDetails.setTotalWorkingDays(workingDays);
						monthlySalaryDetails.setPaidLeave(Integer.parseInt(paidLeave));
						monthlySalaryDetails.setHalfDay(halfDay);
						monthlySalaryDetails.setPresentDays(present);
						monthlySalaryDetails.setBonus(0.0);
						monthlySalaryDetails.setUpdatedWhen(new Timestamp(current.getMillis()));

						monthlySalaryDetailsRepo.save(monthlySalaryDetails);
					}
					if (email != null && !email.isEmpty())
						gmail = email;
					mailService.sendEmail(baos, name, gmail,
							paySlipDetails.get(Util.MONTH) + " " + paySlipDetails.get(Util.YEAR));
					log.info("Mail send successfully this email id=" + gmail);
				} catch (Exception e) {
					log.error("Getting error while payslip generation=" + e.getMessage());
					mailService.sendEmail(name);
					continue;
				}
			} catch (Exception e) {
				log.error("Getting error while payslip generation." + e.getMessage());
				break;
			}
		}

		return "Mail Send Successfully";
	}

	public ByteArrayOutputStream createPdf(String empId, String name, int totalWorkingDays, int present, int leave,
			int halfDay, String salary, String paidLeave, String date, String bankName, String accountNumber,
			String designation, String joiningDate, int adhoc1, String payPeriod, String esic, String pf,
			int adjustment, int medicalInsurance, int tds, MonthlySalaryDetails monthlySalaryDetails)
			throws IOException, DocumentException {

		float pfAmount = 0;
		double grossSalary = Double.parseDouble(salary);
		double employerPf = (double) (Math.round(((grossSalary / 2) * 0.13)));
		double employeeESICAmount = 0;
		double employerESICAmount = 0;

		if (esic.equalsIgnoreCase("Yes") && pf.equalsIgnoreCase("Yes")) {
			
			employerESICAmount = Double.valueOf(Math.round(grossSalary * (0.0325)));
			employeeESICAmount = Double.valueOf (Math.round(grossSalary * (0.0075)));
			grossSalary = Math.round(
					grossSalary - employerPf - (employeeESICAmount + employerESICAmount) + (grossSalary * 0.01617));
		} else if (esic.equalsIgnoreCase("No") && pf.equalsIgnoreCase("Yes")) {

			grossSalary = Math.round(grossSalary - employerPf + (grossSalary * 0.01617));
		}

		double basic = Math.round(grossSalary / 2);
		double hra = Math.round(grossSalary / 2);
		int yourWorkingDays = present + Integer.parseInt(paidLeave);
		double amountPerDay = grossSalary / totalWorkingDays;
		double unpaidLeave = totalWorkingDays - present;
		monthlySalaryDetails.setAbsentDays((int) unpaidLeave);
		unpaidLeave -= Integer.parseInt(paidLeave);
		unpaidLeave *= amountPerDay;
		double HalfDays = halfDay * amountPerDay / 2;
		double netAmount = Math.round((yourWorkingDays * amountPerDay) - HalfDays);
		netAmount = Math.round(netAmount + adhoc1);
		if (netAmount < 0) {
			netAmount = 0;
			adhoc1 = 0;
		}

		if (esic.equalsIgnoreCase("yes") && netAmount != 0) {
			employeeESICAmount = (double) (Math.round(grossSalary * (0.0075)));

		}

		if (pf.equalsIgnoreCase("yes") && netAmount != 0) {
			pfAmount = (float) (Math.round(basic * 0.120));
		}
		double halfDayAmount = ((double) halfDay / 2) * amountPerDay;
//		double grossDeduction = employerESICAmount + pfAmount + (unpaidLeave - halfDayAmount) + adjustment + medicalInsurance
//				+ tds;
		double grossDeduction = employeeESICAmount + pfAmount + (unpaidLeave - halfDayAmount) + adjustment
				+ medicalInsurance + tds;
		double employerEsic = employerESICAmount;
		double employeePf = pfAmount;
		double td = tds;
		netAmount -= employeeESICAmount;
		netAmount -= pfAmount;
		netAmount = Math.round(netAmount);
		netAmount -= medicalInsurance;
		netAmount -= adjustment;
		monthlySalaryDetails.setHouseRentAllowance(hra);
		monthlySalaryDetails.setBasic(basic);
		monthlySalaryDetails.setGrossSalary(grossSalary);
		monthlySalaryDetails.setGrossDeduction(grossDeduction);
		monthlySalaryDetails.setEmployerESICAmount(employerEsic);
		monthlySalaryDetails.setEmployeeESICAmount(employeeESICAmount);
		monthlySalaryDetails.setEmployeePFAmount(employeePf);
		monthlySalaryDetails.setEmployerPFAmount(employerPf);

		monthlySalaryDetails.setUnpaidLeave((int) unpaidLeave);
		monthlySalaryDetails.setNetSalary(netAmount);
		monthlySalaryDetails.setTds(td);
		ByteArrayOutputStream byteArrayOutputStream = DetailedSalarySlip.builder().build()
				.generateDetailedSalarySlipPDF(empId, name, totalWorkingDays, present, leave, halfDay, salary,
						paidLeave, date, bankName, accountNumber, designation, joiningDate, adhoc1, payPeriod,
						employeeESICAmount, pfAmount, netAmount, grossSalary, basic, hra, amountPerDay, unpaidLeave,
						adjustment, medicalInsurance, tds);
		return byteArrayOutputStream;
	}

	@Override
	public ViewPaySlipDto viewPay(int empId, String month, String year) throws Exception {
		log.info("viewPaySlipByEmpId : info level log msg");
		ViewPaySlipDto viewPaySlipDto = new ViewPaySlipDto();
		try {

			validateMonthYear(month, year);
			SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
			SimpleDateFormat outputFormat = new SimpleDateFormat("MM"); // 01-12

			Calendar cal = Calendar.getInstance();
			cal.setTime(inputFormat.parse(month));

			String monthDate = String.valueOf(outputFormat.format(cal.getTime()));

			String firstDayMonth = "01/" + monthDate + "/" + year;
			String lastDayOfMonth = (LocalDate.parse(firstDayMonth, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
					.with(TemporalAdjusters.lastDayOfMonth())).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

			String payPeriod = firstDayMonth + " - " + lastDayOfMonth;

			Optional<User> user = Optional.ofNullable(userRepo.findByEmployeeId(empId)
					.orElseThrow(() -> new NoDataFoundException("Employee not found with EmpId:" + empId)));

			Optional<EmpPayrollDetails> empPayrollDetails = Optional
					.ofNullable(empPayrollDetailsRepo.findByEmployeeId(empId)
							.orElseThrow(() -> new NoDataFoundException("Employee not found with EmpId:" + empId)));

			String name = user.get().getFirstName() + " " + user.get().getLastName();

			List<MonthlySalaryDetails> monthlySalaryDetailsList = monthlySalaryDetailsRepo
					.findByEmpIdAndMonthAndYear(empId, month.toUpperCase());

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			MonthlySalaryDetails monthlySalary = monthlySalaryDetailsList.stream()
					.filter(monthlySalaryDetails -> Integer
							.valueOf(LocalDate.parse(monthlySalaryDetails.getCreditedDate(), formatter).getYear())
							.equals(Integer.parseInt(year)))
					.findFirst().orElseThrow(() -> new NoDataFoundException("PaySlip Details of EmpId:" + empId
							+ " Not found for the Month:" + month + " & Year:" + year));

			viewPaySlipDto.setAccountNo(empPayrollDetails.get().getAccountNumber());
			viewPaySlipDto.setAdhoc(monthlySalary.getAdhoc());
			viewPaySlipDto.setCreditedDate(monthlySalary.getCreditedDate());
			viewPaySlipDto.setDesignation(empPayrollDetails.get().getDesignation());
			viewPaySlipDto.setEmpName(name);
			viewPaySlipDto.setEmpTotalWorkingDays(monthlySalary.getPresentDays());
			viewPaySlipDto.setGrossSalary(monthlySalary.getGrossSalary());
			viewPaySlipDto.setLeavesTaken(monthlySalary.getAbsentDays());
			viewPaySlipDto.setNetAmountPayable(monthlySalary.getNetSalary());
			viewPaySlipDto.setOfficeTotalWorkingDays(monthlySalary.getTotalWorkingDays());
			viewPaySlipDto.setPayPeriods(payPeriod);

		} catch (Exception e) {
			e.printStackTrace();
			log.error("viewPaySlipByEmpId : info level log msg" + e.getMessage());
			throw e;
		}
		return viewPaySlipDto;
	}

	private void validateMonthYear(String month, String year) throws IllegalArgumentException, ParseException {
		SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
		Calendar inputCalendar = Calendar.getInstance();
		inputCalendar.setTime(inputFormat.parse(month));
		int inputMonth = inputCalendar.get(Calendar.MONTH);
		int inputYear = Integer.parseInt(year);

		Calendar currentCalendar = Calendar.getInstance();
		int currentMonth = currentCalendar.get(Calendar.MONTH);
		int currentYear = currentCalendar.get(Calendar.YEAR);

		LocalDate currentDate = LocalDate.now();

		String currentMonthName = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
		int currentYearName = currentDate.getYear();

		if (inputYear > currentYear || (inputYear == currentYear && inputMonth > currentMonth)) {
			throw new IllegalArgumentException(
					"The InputMonth:" + month + " and InputYear:" + year + " cannot be Greater than the CurrentMonth:"
							+ currentMonthName + " and CurrentYear:" + currentYearName);
		}
	}



	@Override
	public String updateNetAmountInExcel(MultipartFile file) throws IOException {

		String salary = "", paidLeave = "", sheetName = "";
		int NetAmount = 0, adhoc1 = 0, adhoc2 = 0, adhoc3 = 0, workingDays = 0, present = 0, halfDay = 0;

		Map<String, Integer> excelColumnName = new HashMap<>();
		String projDir = System.getProperty("user.dir");
		NumberFormat format = NumberFormat.getInstance();
		XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
		DataFormatter dataFormatter = new DataFormatter();

		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			XSSFSheet sh = workbook.getSheetAt(i);
			if (sh.getLastRowNum() > 0) {
				sheetName = sh.getSheetName();
			}
		}

		XSSFSheet sheet = workbook.getSheet(sheetName);

		Row headerRow = sheet.getRow(0);

		int columnCount = headerRow.getLastCellNum();
		String columnHeader = "";
		for (int i = 0; i < columnCount; i++) {
			headerRow.getCell(i);
			headerRow.getCell(i).getStringCellValue();
			columnHeader = String.valueOf(headerRow.getCell(i)).trim();

			excelColumnName.put(columnHeader, i);
		}
		for (int i = 2; i <= 50; i++) {
			try {
				XSSFRow row = sheet.getRow(i);
				try {
					workingDays = Integer.parseInt(
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.TotalWorkingDays))));
					present = Integer.parseInt(
							dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.YourWorkingDays))));
					halfDay = Integer
							.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.HalfDay))));
					salary = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.salary)));
					paidLeave = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.PaidLeave)));

					adhoc1 = Integer
							.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc1))));
					adhoc2 = Integer
							.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc2))));
					adhoc3 = Integer
							.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc3))));
					double netAmount = calculateNetAmount(workingDays, present, salary, paidLeave, halfDay, adhoc1,
							adhoc2, adhoc3);
					row.createCell(excelColumnName.get(Util.NetAmount)).setCellValue(netAmount);

				} catch (Exception e) {
					continue;
				}
			} catch (Exception e) {
				break;
			}
		}
		FileOutputStream fileOutputStream = new FileOutputStream(
				"C:/Users/hp/Desktop/excel/" + file.getOriginalFilename());
		workbook.write(fileOutputStream);
		fileOutputStream.close();

		return "done";
	}

	public float calculateNetAmount(int totalWorkingDays, int present, String salary, String paidLeave, int halfDay,
			int adhoc1, int adhoc2, int adhoc3) {
		float grossSalary = Float.valueOf(salary);
		int yourWorkingDays = present + Integer.parseInt(paidLeave);
		float amountPerDay = grossSalary / totalWorkingDays;
		float HalfDays = halfDay * amountPerDay / 2;
		float netAmount = (yourWorkingDays * amountPerDay) - HalfDays;
		netAmount = netAmount + adhoc1 + adhoc2 + adhoc3;
		return netAmount;
	}

	public boolean checkEmpDetails(String empId, String gmail, String accountNumber, List<User> employees, String fname,
			String lName) {
		log.info("validating the columns value Gmail {},  AccountNumber{}, FirstName {}, LastName {}", gmail, accountNumber,
				fname, lName);
		adtID="";
		int userId = Integer.parseInt(empId);
		boolean flag = true;
		Optional<User> employee = employees.stream().filter(user -> user.getId() == userId).findFirst();
		if (employee != null && !employee.isEmpty()) {
			String[] fullName = employee.get().getLastName().split(" ");
			String lname = fullName[0].toString();
			if (employee.get().getEmail().trim().equalsIgnoreCase(gmail)
					&& employee.get().getFirstName().trim().equalsIgnoreCase(fname)
					&& lname.trim().trim().equalsIgnoreCase(lName)) {
				EmpPayrollDetails empDetails = empPayrollDetailsRepo.getByEmpId(employee.get().getId());
				String aNo=	empDetails.getAccountNumber();
				for (int i = 0; i < aNo.length(); i++) {
					if (aNo.startsWith("0")) {
						aNo = aNo.substring(1);
					} else {
						break;
					}
				}
				if (aNo.equalsIgnoreCase(accountNumber)) {
					flag = false;
					adtID=employee.get().getAdtId();
					return flag;
				}
				invalidValue = "please enter currect Account Number";
				return flag;

			}
			invalidValue = "please enter correct Email, First name and Last name";
		}

		return flag;
	}

	public boolean isNotNull(String empId, String name, String workingDays, String presentWorkingDays, String leave,
			String halfDay, String salary, String paidLeave, String bankName, String accountNumber, String designation,
			String email, String joiningDate, String esic, String pfAmount, String adjustment, String tds,
			String medicalInsurance, String adhoc, Map<String, String> paySlipDetails) {
		log.info("Verifying columns ");
		int totalDays = Integer.parseInt(paySlipDetails.get(Util.WORKING_DAY));

		invalidValue = "] fields are missing or null. Kindy fill correct information !!";

		allFieldeValue = 0;
		if (empId.isEmpty() || empId == null) {
			invalidValue = ",employeeId " + invalidValue;
			allFieldeValue++;

		}
		if (name.isEmpty() || name == null) {
			invalidValue = ",name" + invalidValue;
			allFieldeValue++;

		}
		if (workingDays.isEmpty() || workingDays == null || Integer.parseInt(workingDays) > totalDays
				|| Integer.parseInt(workingDays) != totalDays) {
			invalidValue = ",workingDay" + invalidValue;
			allFieldeValue++;
		}

		if (presentWorkingDays.isEmpty() || presentWorkingDays == null) {
			invalidValue = ",presentDay" + invalidValue;
			allFieldeValue++;

		}

		if (leave.isEmpty() || leave == null) {
			invalidValue = ",leave" + invalidValue;
			allFieldeValue++;
		}
		if (halfDay.isEmpty() || halfDay == null) {
			invalidValue = ",halfDay" + invalidValue;
			allFieldeValue++;
		}

		if (salary.isEmpty() || salary == null) {
			invalidValue = ",salary" + invalidValue;
			allFieldeValue++;
		}
		if (paidLeave.isEmpty() || paidLeave == null) {
			invalidValue = ",paidLeave" + invalidValue;
			allFieldeValue++;
		}

		if (bankName.isEmpty() || bankName == null) {
			invalidValue = ",bankName" + invalidValue;
			allFieldeValue++;
		}
		if (accountNumber.isEmpty() || accountNumber == null) {
			invalidValue = ",accountNumber" + invalidValue;
			allFieldeValue++;
		}

		if (designation.isEmpty() || designation == null) {
			invalidValue = ",designation" + invalidValue;
			allFieldeValue++;
		}
		if (email.isEmpty() || email == null) {
			invalidValue = ",email" + invalidValue;
			allFieldeValue++;
		}

		if (joiningDate.isEmpty() || joiningDate == null) {
			invalidValue = ",joiningDate " + invalidValue;
			allFieldeValue++;
		}

		if (esic.isEmpty() || esic == null) {
			invalidValue = ",esic " + invalidValue;
			allFieldeValue++;
		}

		if (pfAmount.isEmpty() || pfAmount == null) {
			invalidValue = ",pfAmount " + invalidValue;
			allFieldeValue++;
		}

		if (adjustment.isEmpty() || adjustment == null) {
			invalidValue = ",adjustment " + invalidValue;
			allFieldeValue++;
		}

		if (tds.isEmpty() || tds == null) {

			invalidValue = ",tds " + invalidValue;
			allFieldeValue++;

		}
		if (medicalInsurance.isEmpty() || medicalInsurance == null) {
			invalidValue = ",medicalInsurance " + invalidValue;
			allFieldeValue++;
		}

		if (adhoc.isEmpty() || adhoc == null) {
			invalidValue = ",adhoc " + invalidValue;
			allFieldeValue++;
		}

		if (!invalidValue.equalsIgnoreCase("] fields are missing or null. Kindy fill correct information !!")) {
			invalidValue = invalidValue.substring(1);
			invalidValue = "Given [" + invalidValue;
			if (allFieldeValue < 19)
				log.error("Error found=" + invalidValue);
			return true;
		}
		return false;
	}

//  generate salary code modification
	@Override
	public String generatePaySlipForAllEmployees(String emailInput) throws ParseException, IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// int officeTotalWorkingDay = util.getWorkingDays();
		Map<String, String> paySlipDetails = util.getWorkingDaysAndMonth();
	//	List<SalaryDetails> salaryDetailsList = salaryDetailsRepo.findAll();
		List<User> employeeList= userRepo.findAllByIsActive(true);
		String name = null;
		PaySlip paySlip = null;
		
		double adjustment=0;
		Map<ByteArrayOutputStream, String> payslip = new HashMap<>();
		if ((!employeeList.isEmpty()) || (employeeList.size() > 0)) {

			for (User employee : employeeList) {
				try {
					if (employee.getId() != 0) {
						try {
							String fName = employee.getFirstName();
							String lName = employee.getLastName();
							name = fName + " " + lName;
							String adtId= employee.getAdtId()!=null?employee.getAdtId():"";
							Optional<EmpPayrollDetails> empPayrollDetailsOptional = empPayrollDetailsRepo
									.findByEmployeeId(employee.getId());
							if (!empPayrollDetailsOptional.isPresent()) {
								log.info(
										"Employee payroll details are not present. Please enter the employee record "
												+ employee.getId());
								mailService.sendEmail(name, "Employee payroll details are not present. kindly enter the data.");
								continue;
							}
							Optional<SalaryDetails> salaryDetails = salaryDetailsRepo.findByEmployeeId(employee.getId());
							if (salaryDetails.isPresent()) {
								paySlip = new PaySlip();	
								String gmail = employee.getEmail();
								double medical=salaryDetails.get().getMedicalInsurance()!=null?salaryDetails.get().getMedicalInsurance():0;
								double hra=salaryDetails.get().getHouseRentAllowance()!=null?salaryDetails.get().getHouseRentAllowance():0;
								double basic=salaryDetails.get().getBasic()!=null?salaryDetails.get().getBasic():0;
								double emppf =salaryDetails.get().getEmployeePFAmount()!=null?salaryDetails.get().getEmployeePFAmount():0;
								double employerpf =salaryDetails.get().getEmployerPFAmount()!=null?salaryDetails.get().getEmployerPFAmount():0;
								double grossAmount =salaryDetails.get().getGrossSalary()!=null?salaryDetails.get().getGrossSalary():0;
								double salary = empPayrollDetailsOptional.get().getSalary()!=null?empPayrollDetailsOptional.get().getSalary():0;

								// null checks
								if (nullValidation(adtId, name, employee.getEmail(),
										basic, grossAmount,hra,	empPayrollDetailsOptional.get().getAccountNumber(),
										empPayrollDetailsOptional.get().getBankName(),
										empPayrollDetailsOptional.get().getDesignation(),
										empPayrollDetailsOptional.get().getJoinDate(),
										Integer.parseInt(paySlipDetails.get("workingDays")))) {

									if(salary<=0) {
										log.info(" Employee salary can't be null", employee.getId());
										mailService.sendEmail(name, "salary can't be null for the mention employee");
										continue;
									}
									boolean isESIC = false;
									if (salary <= 21000) {
										isESIC = true;
									}

									double calculatedGross = grossSalaryCalculation(empPayrollDetailsOptional.get(),
											basic, salaryDetails.get(), isESIC, name);
									if (calculatedGross == -1) {
										continue;
									}
									double empGrossSalaryAmount = grossAmount;
									double grossSalaryDifference = Math.round(calculatedGross - empGrossSalaryAmount);

									if (grossSalaryDifference > 100) {
										log.info("Gross salary calculation is not correct.");
										mailService.sendEmail(name, "The gross salary difference amount is: "
												+ grossSalaryDifference
												+ " ,while the gross salary amount retrived from database is: "
												+ empGrossSalaryAmount
												+ " and the calculated & validated gross salary amount is: "
												+ calculatedGross
												+ " ,please check entered salary details & enter the correct gross salary amount for the mentioned employee");
										continue;
									}
									
									double totalLeaveDeduction = calculateAndUpdateEmployeeTotalLeaves(
											salaryDetails.get().getEmpId(), empGrossSalaryAmount,
											paySlipDetails.get(Util.MONTH), paySlipDetails.get(Util.YEAR),
											Integer.parseInt(paySlipDetails.get(Util.WORKING_DAY)), paySlip, name);

									if (totalLeaveDeduction == -1) {
										log.info("Employees leave balance record is not exist. please enter the data.",
												salaryDetails.get().getEmpId());
										continue;
									}
									
									Double grossEarning = empGrossSalaryAmount;
									Double grossDeductionCal = (salaryDetails.get().getEmployeeESICAmount()!=null?salaryDetails.get().getEmployeeESICAmount():0)
											+ emppf + paySlip.getLeaveDeductionAmount()
											+ medical;
									double grossDeduction=grossDeductionCal;
//									double grossDeduction = Math.round(grossDeductionCal) <= Math.round(grossEarning)
//											? Math.round(grossDeductionCal)
//											: Math.round(grossEarning);
									
//									double empNetSalaryAmount = Math
//											.round(empGrossSalaryAmount - (salaryDetails.get().getEmployeeESICAmount()
//													+ salaryDetails.get().getEmployeePFAmount()
//													+ salaryDetails.get().getMedicalInsurance()));
									
									double empNetSalaryAmount = Math
											.round(grossEarning - grossDeductionCal);
									if (empNetSalaryAmount < 0) {
										empNetSalaryAmount = 0;
									}
							
									Month month = Month.valueOf(paySlipDetails.get(util.MONTH).toUpperCase());
									Optional<ExpenseItems> items = expenseManagementRepo.findExpenseDetailsByEmpId(employee.getId(), month.getValue(), Integer.valueOf(paySlipDetails.get(util.YEAR)));
									double adhoc=0;
									if (items.isPresent()) {
										if (items.get().getStatus().equalsIgnoreCase("Approved")) {
											adhoc = Double.valueOf(items.get().getAmount());
											empNetSalaryAmount = Math.round(empNetSalaryAmount + adhoc);
										}
									}

									paySlip.setGrossSalary(grossEarning.floatValue());
									paySlip.setAccountNumber(empPayrollDetailsOptional.get().getAccountNumber());
									paySlip.setBankName(empPayrollDetailsOptional.get().getBankName());
									paySlip.setJobTitle(empPayrollDetailsOptional.get().getDesignation());
									paySlip.setName(name);
									paySlip.setTotalWorkingDays(Integer.parseInt(paySlipDetails.get(Util.WORKING_DAY)));
									paySlip.setPayPeriods(paySlipDetails.get(Util.PAY_PERIOD));
									paySlip.setNetSalaryAmount(empNetSalaryAmount);
									paySlip.setSalary(empGrossSalaryAmount);
									paySlip.setAdhoc(adhoc);
									paySlip.setGrossDeduction(grossDeduction);
									//paySlip.setAdjustment(adjustment);

									baos = DetailedSalarySlip.builder().build().generateDetailedSalarySlipPDF(adtId,
											salaryDetails.get(), paySlip, empPayrollDetailsOptional.get().getJoinDate(),
											paySlipDetails.get(Util.MONTH), adjustment);

									if (!emailInput.isEmpty() && !emailInput.isBlank()) {
										payslip.put(baos, name);
									}

									log.info("baos:---createPDF");

								} else {
									continue;
								}
								if (emailInput.isEmpty()) {
									mailService.sendEmail(baos, name, gmail,
											paySlipDetails.get(Util.MONTH) + " " + paySlipDetails.get(Util.YEAR));

									MonthlySalaryDetails saveMonthlySalaryDetails = new MonthlySalaryDetails();

									saveMonthlySalaryDetails(saveMonthlySalaryDetails, salaryDetails.get(), paySlipDetails,
											paySlip);
								}
							}else {
								mailService.sendEmail(name, "Salary details are not exist for the employee. Kindly enter salary details.");
								continue;
							}

						} catch (Exception e) {
							e.printStackTrace();
							mailService.sendEmail(name, "Error while generating payslip.");
							log.info("e.printStackTrace()---" + e.getMessage());
							continue;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					mailService.sendEmail(name, "Kindly Check the salary details for mentioned Employee.");
					log.info("e.printStackTrace()----" + e.getMessage());
					break;
				}
			}

			if (!emailInput.isEmpty() && !emailInput.isBlank()) {
				 Optional<User> receiverName= userRepo.findByEmail(emailInput);
				mailService.sendEmail(payslip, receiverName.get().getFirstName()+" "+receiverName.get().getLastName(), emailInput,
						paySlipDetails.get(Util.MONTH) + " " + paySlipDetails.get(Util.YEAR));
			}
		}
		return "Mail Send Successfully";
	}

	private void saveMonthlySalaryDetails(MonthlySalaryDetails saveMonthlySalaryDetails, SalaryDetails salaryDetails,
			Map<String, String> paySlipDetails, PaySlip paySlip) {
		try {
			log.info(
					"PayRollServiceImpl: generatePaySlipForAllEmployees: saveMonthlySalaryDetails info level log message");
	
			saveMonthlySalaryDetails.setEmpId(salaryDetails.getEmpId());
			saveMonthlySalaryDetails.setBasic(salaryDetails.getBasic());
			saveMonthlySalaryDetails.setEmployeeESICAmount(salaryDetails.getEmployeeESICAmount());
			saveMonthlySalaryDetails.setEmployerESICAmount(salaryDetails.getEmployerESICAmount());
			saveMonthlySalaryDetails.setEmployeePFAmount(salaryDetails.getEmployeePFAmount());
			saveMonthlySalaryDetails.setEmployerPFAmount(salaryDetails.getEmployerPFAmount());
			saveMonthlySalaryDetails.setMedicalInsurance(salaryDetails.getMedicalInsurance());
			saveMonthlySalaryDetails.setTds(salaryDetails.getTds());
			saveMonthlySalaryDetails.setGrossSalary(paySlip.getGrossSalary().doubleValue());
			saveMonthlySalaryDetails.setNetSalary(paySlip.getNetSalaryAmount());
			saveMonthlySalaryDetails.setAdhoc(salaryDetails.getAdhoc());
			saveMonthlySalaryDetails.setAdjustment(salaryDetails.getAdjustment());
			saveMonthlySalaryDetails.setHouseRentAllowance(salaryDetails.getHouseRentAllowance());
			saveMonthlySalaryDetails.setDearnessAllowance(salaryDetails.getDearnessAllowance());
			saveMonthlySalaryDetails.setGrossDeduction(paySlip.getGrossDeduction());
			saveMonthlySalaryDetails.setAbsentDeduction(paySlip.getLeaveDeductionAmount());
			saveMonthlySalaryDetails.setCreditedDate(util.getCreatedDate(15));
			saveMonthlySalaryDetails.setMonth(paySlipDetails.get(Util.MONTH));
			saveMonthlySalaryDetails.setBonus(salaryDetails.getBonus());
			saveMonthlySalaryDetails.setPresentDays(paySlip.getYouWorkingDays());
			saveMonthlySalaryDetails.setAbsentDays(paySlip.getNumberOfLeavesTaken());
			saveMonthlySalaryDetails.setTotalWorkingDays(paySlip.getTotalWorkingDays());
			saveMonthlySalaryDetails.setHalfDay(paySlip.getHalfday());
			saveMonthlySalaryDetails.setPaidLeave(paySlip.getPaidLeave());
			saveMonthlySalaryDetails.setUnpaidLeave(paySlip.getUnpaidLeave());
			saveMonthlySalaryDetails.setActive(true);
			DateTimeZone istTimeZone = DateTimeZone.forID("Asia/Kolkata");
			DateTime current = new DateTime(istTimeZone);
			saveMonthlySalaryDetails.setUpdatedWhen(new Timestamp(current.getMillis()));
;
			monthlySalaryDetailsRepo.save(saveMonthlySalaryDetails);

		} catch (Exception e) {
			e.printStackTrace();
			log.error(
					"PayRollServiceImpl: generatePaySlipForAllEmployees: saveMonthlySalaryDetails: e.printStackTrace()---"
							+ e.getMessage());
		}
	}
	
	// null checks for values
		private boolean nullValidation(String empAdtId, String name, String gmail, double basic, double grossSalary, double hRA,
				String accountNumber, String bankName, String designation, String joiningDate, int officeTotalWorkingDay) {
			log.info(" nullValidation : Validating fields value :");
			String msg="";
			StringBuilder msgBuilder= new StringBuilder(msg);
			if (empAdtId.isEmpty() || empAdtId== null) {
				msgBuilder.append("Employee Adt id can't be empty. \n");
			} 
			if(name.isEmpty() || name == null) {
				msgBuilder.append("Employee name can't be empty. \n");
			}  
			if(officeTotalWorkingDay < 0 ) {
				msgBuilder.append("Office working days can't be empty. \n");
			}
			if(bankName.isEmpty() || bankName == null) {
				msgBuilder.append("Bank name can't be empty. \n");
			} 
			
			if(accountNumber == null || accountNumber.isEmpty()) {
				msgBuilder.append("Account Number can't be empty. \n");

			} 
			if(designation.isEmpty() || designation == null) {
				msgBuilder.append("Designation can't be empty. \n");
			} 
			if(joiningDate.isEmpty() || joiningDate == null) {
				msgBuilder.append("Joining Date can't be empty. \n");
			} 
			if(basic <= 0) {
				msgBuilder.append("Basic can't be empty. \n");
			}
			if(gmail.isEmpty() || gmail == null){
				msgBuilder.append("Gmail Id can't be empty. \n");
			}
			 if(grossSalary <= 0 ) {
				msgBuilder.append("Gross salary can't be empty. \n");
			 }
			if(hRA <= 0) {
				msgBuilder.append("HRA can't be empty. \n");
			}
			
			if(!msgBuilder.toString().equalsIgnoreCase(msg)) {
				mailService.sendEmail(name, msgBuilder.toString());
				return false;
			}
			return true;
		}

	// Leave and leave deduction calculation
	private double calculateAndUpdateEmployeeTotalLeaves(int empId, double empGrossSalary, String month, String year,
			int officeTotalWorkingDay, PaySlip paySlip, String name) throws ParseException, IOException {

		double absentDeductionAmt = 0, halfDayAmount = 0, halfDayAmountDeduct = 0, totalLeaveDeduction = 0;
		try {
			double amountPerDay = empGrossSalary / officeTotalWorkingDay;
			int empRemainingLeave = 0;
			int empPaidLeave = 0;
			int empUnpaidLeave = 0;
			int empTotalWorkingDay = timeSheetRepo.findEmpTotalWorkingDayCount(empId, month, year);
			int empHalfDay = timeSheetRepo.findEmpTotalHalfDayCount(empId, month, year);
			if (empHalfDay > 0) {
				empTotalWorkingDay = empTotalWorkingDay + empHalfDay;
			}
			if(empTotalWorkingDay>officeTotalWorkingDay) {
				mailService.sendEmail(name, "Discrepancy found: Employee working days are greater than office working days.Kindly check the time sheet for mentioned employee.");
				return -1;
			}
					
			int empLeave = officeTotalWorkingDay - empTotalWorkingDay;

			Optional<LeaveBalance> leaveBalanceOptional = leaveBalanceRepo.findByEmployeeId(empId);
			if (!leaveBalanceOptional.isPresent()) {
				mailService.sendEmail(name, "Leave balance is not Exist for the employee.");
				return -1;
			}

			int leaveBal = leaveBalanceOptional.get().getLeaveBalance();

			if (leaveBal >= empLeave) {
				empRemainingLeave = leaveBal - empLeave;

				leaveBal = empRemainingLeave;
				empPaidLeave = empLeave;
				empUnpaidLeave = 0;

			} else if (empLeave > leaveBal) {
				empRemainingLeave = empLeave - leaveBal;

				empPaidLeave = leaveBal;
				leaveBal = 0;
				empUnpaidLeave = empRemainingLeave;

				absentDeductionAmt = Math.round(amountPerDay * empUnpaidLeave);
			}

			if (empHalfDay > 0) {
				halfDayAmount = (amountPerDay / 2);
				halfDayAmountDeduct = Math.round(empHalfDay * halfDayAmount);
			}
			
			// update Allleaves in db-----------
		//	leaveBalanceRepo.updateAllLeavesByEmpId(empId, leaveBal, empPaidLeave, empUnpaidLeave, empHalfDay);
			leaveBalanceRepo.updateLeaveBalByEmpId(empId, leaveBal);
			totalLeaveDeduction = Math.round(halfDayAmountDeduct + absentDeductionAmt);
			paySlip.setNumberOfLeavesTaken(empLeave);
			paySlip.setYouWorkingDays(empTotalWorkingDay);
			paySlip.setPaidLeave(empPaidLeave);
			paySlip.setUnpaidLeave(empUnpaidLeave);
			paySlip.setHalfday(empHalfDay);
			paySlip.setLeaveDeductionAmount(totalLeaveDeduction);
			return totalLeaveDeduction;
		} catch (Exception e) {
			log.error("Error occured while calculating leave & leave deduction " + e.getMessage());
			mailService.sendEmail(name, "Kindly check Leave balance details for the employee.");
			return -1;
		}
	}

	// gross salary calculation for verification
	private double grossSalaryCalculation(EmpPayrollDetails empPayrollDetails, double fixedBasic,
			SalaryDetails salaryDetails, boolean isESIC, String name) {
		double salary = empPayrollDetails.getSalary();
		double actualBasic = salary / 2;
		double grossSalaryAmount = salary;
		// employer pf and esic portion calculation 13% and 3.25% respectively
		double employerPFAmount = Math.round(actualBasic * 0.13);
		double employerESICAmount = Math.round(grossSalaryAmount * 0.0325);
		double employeeESICAmount = Math.round(grossSalaryAmount * 0.0075);
		String msg = "";
		double esicAmt = salaryDetails.getEmployerESICAmount()!=null?salaryDetails.getEmployerESICAmount():0;
		double pfEmployer= salaryDetails.getEmployerPFAmount()!=null?salaryDetails.getEmployerPFAmount():0;
		if (isESIC) {
			if (Math.round(employerESICAmount - esicAmt) > 100) {

				msg = "employer ESIC amount difference is: "
						+ Math.round(employerESICAmount - esicAmt)
						+ " ,while the employer ESIC amount retrived from database is: "
						+ esicAmt
						+ " & the calculated & validated employer ESIC amount is: " + employerESICAmount
						+ " ,please check entered salary details & enter the correct employer ESIC amount for the mentioned employee";
			}	
		}
		if (Math.round(employerPFAmount - pfEmployer) > 100) {

			msg = "employer PF amount difference is: "
					+ Math.round(employerPFAmount - pfEmployer)
					+ ",while the employer PF amount retrived from database is: " + pfEmployer
					+ " & the calculated & validated employer PF amount is: " + employerPFAmount
					+ " ,please check entered salary details & enter the correct employer PF amount for the mentioned employee";
		}
		
		//calculating gross salary of an employee (deduction of employer portion)		
		if (msg.isEmpty()) {
			if (isESIC) {
				grossSalaryAmount = Math.round(grossSalaryAmount - employerPFAmount
						- (employeeESICAmount + employerESICAmount) + (grossSalaryAmount * 0.01617));
				// calculating employee esic
				employeeESICAmount=Math.round(grossSalaryAmount * 0.0075);
				double esicEmployee =salaryDetails.getEmployeeESICAmount()!=null?salaryDetails.getEmployeeESICAmount():0;
				if (Math.round(employeeESICAmount - esicEmployee) > 100) {

					msg = "employee ESIC amount difference is: "
							+ Math.round(employeeESICAmount - esicEmployee)
							+ " ,while the employee ESIC amount retrived from database is: "
							+ esicEmployee
							+ " & the calculated & validated employee ESIC amount is: " + employeeESICAmount
							+ " ,please check entered salary details & enter the correct employee ESIC amount for the mentioned employee";
				}
			} else {
				if (fixedBasic == 15000) {
					grossSalaryAmount = Math
							.round(grossSalaryAmount - (fixedBasic * 0.13) + (grossSalaryAmount * 0.01617));
				} else {
					grossSalaryAmount = Math
							.round(grossSalaryAmount - employerPFAmount + (grossSalaryAmount * 0.01617));
				}
			}
			
			double pf=salaryDetails.getEmployeePFAmount()!=null?salaryDetails.getEmployeePFAmount():0;
			msg = validateEmployeePF(grossSalaryAmount, pf);
		}
		if (!msg.isEmpty()) {
			mailService.sendEmail(name, msg);
			return -1;
		}
		return grossSalaryAmount;
	}

	private String validateEmployeePF(double calculatedGross, double employeePFAmount) {
		double basic = calculatedGross / 2;
		double empCalcutedPFAmount = Math.round(basic * 0.12);
		if (Math.round(empCalcutedPFAmount - employeePFAmount) > 100) {
			return "employee PF amount difference is: " + Math.round(empCalcutedPFAmount - employeePFAmount)
			+ " ,while the employee PF amount retrived from database is: " + employeePFAmount
			+ " and the calculated & validated employee PF amount is: " + empCalcutedPFAmount
			+ " ,please check entered salary details & enter the correct employee PF amount for the mentioned employee";
		}
		return "";
	}

	@Override
	public SalaryDetailsDTO getEmployeePayrollSalaryDetailsByEmpId(Integer empId) {
		SalaryDetailsDTO salaryDetailsDTO = new SalaryDetailsDTO();
		Optional<EmpPayrollDetails> empPayrollOptional = empPayrollDetailsRepo.findByEmployeeId(empId);
		Optional<SalaryDetails> salaryDetailsOptional = salaryDetailsRepo.findByEmployeeId(empId);
		salaryDetailsDTO.setEmpId(empId);
		try {
			if (empPayrollOptional.isPresent()) {

				salaryDetailsDTO.setSalary(empPayrollOptional.get().getSalary());
				salaryDetailsDTO.setBankName(empPayrollOptional.get().getBankName());
				salaryDetailsDTO.setDesignation(empPayrollOptional.get().getDesignation());
				salaryDetailsDTO.setJoinDate(empPayrollOptional.get().getJoinDate());
				salaryDetailsDTO.setAccountNumber(empPayrollOptional.get().getAccountNumber());
				salaryDetailsDTO.setIfscCode(empPayrollOptional.get().getIfscCode());

				if (salaryDetailsOptional.isPresent()) {

					salaryDetailsDTO.setBasic(salaryDetailsOptional.get().getBasic());
					salaryDetailsDTO.setHouseRentAllowance(salaryDetailsOptional.get().getHouseRentAllowance());
					salaryDetailsDTO.setEmployeeESICAmount(salaryDetailsOptional.get().getEmployeeESICAmount());
					salaryDetailsDTO.setEmployerESICAmount(salaryDetailsOptional.get().getEmployerESICAmount());
					salaryDetailsDTO.setEmployeePFAmount(salaryDetailsOptional.get().getEmployeePFAmount());
					salaryDetailsDTO.setEmployerPFAmount(salaryDetailsOptional.get().getEmployerPFAmount());
					salaryDetailsDTO.setMedicalInsurance(salaryDetailsOptional.get().getMedicalInsurance());
					salaryDetailsDTO.setGrossSalary(salaryDetailsOptional.get().getGrossSalary());
					salaryDetailsDTO.setNetSalary(salaryDetailsOptional.get().getNetSalary());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return salaryDetailsDTO;
	}

	@Override
	public ResponseEntity<Object> validateAmount(Integer empid, SalaryDTO dto) throws ParseException, IOException {
		log.info("Validate entered data for employee {}", empid);
		MonthlySalaryDetails regeneratedSalary = new MonthlySalaryDetails();

		SalaryDetails salaryDetails = salaryDetailsRepo.findByEmployeeId(empid).get();
		double grossAmount= salaryDetails.getGrossSalary();


		if(salaryDetails.getBasic() < dto.getBasic())
			return new ResponseEntity<>(Util.Basic+Util.PAYSLIP_VALIDATION_MSG.replace("[value]", salaryDetails.getBasic().toString())+dto.getBasic(), HttpStatus.NOT_ACCEPTABLE);

		if(salaryDetails.getHouseRentAllowance()< dto.getHra())
			return new ResponseEntity<>(Util.Hra+Util.PAYSLIP_VALIDATION_MSG.replace("[value]",salaryDetails.getHouseRentAllowance().toString())+dto.getHra(), HttpStatus.OK);

		if(salaryDetails.getEmployeeESICAmount() < dto.getEmployeeEsic())
			return new ResponseEntity<>(Util.Esic+Util.PAYSLIP_VALIDATION_MSG.replace("[value]",salaryDetails.getEmployeeESICAmount().toString())+dto.getEmployeeEsic(), HttpStatus.OK);

		if(salaryDetails.getEmployerESICAmount() < dto.getEmployerEsic())
			return new ResponseEntity<>(Util.Esic+Util.PAYSLIP_VALIDATION_MSG.replace("[value]",salaryDetails.getEmployerESICAmount().toString())+dto.getEmployerEsic(), HttpStatus.OK);

		if(salaryDetails.getEmployerPFAmount() < dto.getEmployerPf())
			return new ResponseEntity<>(Util.PF+Util.PAYSLIP_VALIDATION_MSG.replace("[value]",salaryDetails.getEmployerPFAmount().toString())+dto.getEmployerPf(), HttpStatus.BAD_REQUEST);

		if(salaryDetails.getEmployeePFAmount() < dto.getEmployeePf())
			return new ResponseEntity<>(Util.PF+Util.PAYSLIP_VALIDATION_MSG.replace("[value]",salaryDetails.getEmployeePFAmount().toString())+dto.getEmployeePf(), HttpStatus.OK);

//		if( salaryDetails.getBonus() !=null && salaryDetails.getBonus() < dto.getBonus())
//			return new ResponseEntity<>(Util.BONUS+Util.PAYSLIP_VALIDATION_MSG.replace("[value]",salaryDetails.getBonus().toString())+salaryDetails.getBonus(), HttpStatus.OK);

		int empTotalWorkingDay = timeSheetRepo.findEmpTotalWorkingDayCount(empid, dto.getMonth(), dto.getYear());

		int empHalfDay = timeSheetRepo.findEmpTotalHalfDayCount(empid, dto.getMonth(), dto.getYear());
		if (empHalfDay > 0) {
			empTotalWorkingDay = empTotalWorkingDay + empHalfDay;
		}
		int officeTotalWorkingDay = util.getWorkingDays();
		double amountPerDay = Math.round(grossAmount / officeTotalWorkingDay);
		int empLeave = officeTotalWorkingDay - empTotalWorkingDay;
		double absentDeductionAmt = 0;
		Optional<LeaveBalance> leaveBalanceOptional = leaveBalanceRepo.findByEmployeeId(empid);

		int leaveBal = leaveBalanceOptional.get().getLeaveBalance();
		int empRemainingLeave = 0;
		if (leaveBal >= empLeave) {
			empRemainingLeave = leaveBal - empLeave;
			leaveBal = empRemainingLeave;
			regeneratedSalary.setPaidLeave(empLeave);
		} else if (empLeave > leaveBal) {
			leaveBal=1;
			empRemainingLeave = empLeave - leaveBal;
			absentDeductionAmt = amountPerDay * empRemainingLeave;
			regeneratedSalary.setPaidLeave(leaveBal);
			regeneratedSalary.setUnpaidLeave(empRemainingLeave);
		}
		double halfDayAmount = 0;
		double halfDayAmountDeduct = 0;
		if (empHalfDay > 0) {
			halfDayAmount = Math.round(amountPerDay / 2);
			halfDayAmountDeduct = empHalfDay * halfDayAmount;
			absentDeductionAmt = halfDayAmountDeduct + absentDeductionAmt;
		}

		if (absentDeductionAmt < dto.getAbsentDeduction())
			return new ResponseEntity<>(Util.ABSENT_DEDUCTION + Util.PAYSLIP_VALIDATION_MSG + absentDeductionAmt, HttpStatus.OK);
		double grossDeduction =dto.getEmployeeEsic() + dto.getEmployeePf() + absentDeductionAmt + dto.getAjdustment();
		double netSalary = Math.round(salaryDetails.getGrossSalary() - grossDeduction);
		if(netSalary<0) {
			netSalary=0;
		}
		Month month = Month.valueOf(dto.getMonth().toUpperCase());
		//ExpenseItems items = expenseManagementRepo.findExpenseDetailsByEmpId(empid, month.getValue(), Integer.valueOf(dto.getYear()));
		Optional<ExpenseItems> items = expenseManagementRepo.findExpenseDetailsByEmpId(empid, month.getValue(), Integer.valueOf(dto.getYear()));
		if (items.isPresent()) {
			if (items.get().getStatus().equalsIgnoreCase("Approved")) {
				if (items.get().getAmount() < dto.getAdhoc())
					return new ResponseEntity<>(Util.Adhoc + Util.PAYSLIP_VALIDATION_MSG + items.get().getAmount(), HttpStatus.OK);

				if (dto.getAdhoc() != 0) {
					netSalary = netSalary + dto.getAdhoc();
				}
			}
		}

		if (dto.getBonus() != 0) {
		//	grossAmount += dto.getBonus();
			netSalary += dto.getBonus();		
		}
		if (dto.getAdhoc() != 0) {
			//grossAmount += dto.getBonus();
			ExpenseItems adhoc = new ExpenseItems();
			Optional<User> user =userRepo.findByEmployeeId(dto.getEmpId());	
			adhoc.setPaidBy(user.get().getFirstName()+" "+user.get().getLastName());
			adhoc.setAmount(dto.getAdhoc());
			adhoc.setPaymentDate(LocalDate.now());
			adhoc.setPaymentMode("online");
			adhoc.setCategory("");
			adhoc.setCreatedBy("");
			adhoc.setDescription("");
			adhoc.setComments("adhoc amount added.");

			adhoc.setStatus("Approved");
			netSalary += dto.getAdhoc();
			expenseManagementRepo.save(adhoc);
		}

		netSalary = Math.round(netSalary);
		regeneratedSalary.setBasic(dto.getBasic());
		regeneratedSalary.setHouseRentAllowance(dto.getHra());
		regeneratedSalary.setAbsentDeduction(absentDeductionAmt);
		regeneratedSalary.setAdjustment(dto.getAjdustment());
		regeneratedSalary.setAdhoc(dto.getAdhoc());
		regeneratedSalary.setEmployeeESICAmount(dto.getEmployeeEsic());
		regeneratedSalary.setEmployerESICAmount(dto.getEmployerEsic());
		regeneratedSalary.setEmployeePFAmount(dto.getEmployeePf());
		regeneratedSalary.setEmployerPFAmount(dto.getEmployerPf());
		regeneratedSalary.setBonus(dto.getBonus() != null ? dto.getBonus() : 0.0);
		regeneratedSalary.setEmpId(empid);
		regeneratedSalary.setGrossDeduction(grossDeduction);
		regeneratedSalary.setMedicalInsurance(dto.getMedicalAmount() != null ? dto.getMedicalAmount() : 0.0);
		regeneratedSalary.setMonth(dto.getMonth());
		regeneratedSalary.setNetSalary(netSalary);
		regeneratedSalary.setComment(dto.getComment());
		regeneratedSalary.setGrossSalary(salaryDetails.getGrossSalary());
		regeneratedSalary.setCreditedDate(util.getCreatedDate(15));
		regeneratedSalary.setAbsentDays(empLeave);
		regeneratedSalary.setHalfDay(empHalfDay);
		regeneratedSalary.setPresentDays(empTotalWorkingDay);
		regeneratedSalary.setTotalWorkingDays(officeTotalWorkingDay);
		ResponseEntity<Object> response = new ResponseEntity<>(regeneratedSalary, HttpStatus.OK);
		return response;
	}


	@Override
	public String regenerateEmployeePayslip(Integer empid, MonthlySalaryDetails dto) throws DocumentException, IOException {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			LocalDate date = LocalDate.parse(dto.getCreditedDate(), formatter);
			Optional<EmpPayrollDetails> empPayroll = Optional.ofNullable(empPayrollDetailsRepo.findByEmployeeId(empid)
					.orElseThrow(() -> new NoDataFoundException("employee payroll details not found :" + empid)));

			MonthlySalaryDetails existingSalary = monthlySalaryDetailsRepo.findSalaryByEmpidMonth(empid, dto.getMonth(), date.getYear());
			if (existingSalary != null) {
				existingSalary.setActive(false);
				existingSalary.setComment(dto.getComment());				
				monthlySalaryDetailsRepo.save(existingSalary);
			}

			Optional<User> user = Optional.ofNullable(
					userRepo.findById(empid).orElseThrow(() -> new NoDataFoundException("employee not found :" + empid)));
			String name = user.get().getFirstName() + " " + user.get().getLastName();
			//      MonthlySalaryDetails regeneratedSalary= new MonthlySalaryDetails();
			//		regeneratedSalary.setBasic(dto.getBasic());
			//		regeneratedSalary.setHouseRentAllowance(dto.getHouseRentAllowance());
			//		regeneratedSalary.setAbsentDeduction(dto.getAbsentDeduction());
			//		regeneratedSalary.setAdjustment(dto.getAdjustment());
			//		regeneratedSalary.setAdhoc(dto.getAdhoc());
			//		regeneratedSalary.setActive(true);
			//		regeneratedSalary.setEmployeeESICAmount(dto.getEmployeeESICAmount());
			//		regeneratedSalary.setEmployerESICAmount(dto.getEmployerESICAmount());
			//		regeneratedSalary.setEmployeePFAmount(dto.getEmployeePFAmount());
			//		regeneratedSalary.setEmployerPFAmount(dto.getEmployerPFAmount());
			//		regeneratedSalary.setBonus(dto.getBonus());
			//		regeneratedSalary.setEmpId(empid);
			//		regeneratedSalary.setGrossDeduction(dto.getGrossDeduction());
			//		regeneratedSalary.setMedicalInsurance(dto.getMedicalInsurance());
			//		regeneratedSalary.setMonth(dto.getMonth());
			//		regeneratedSalary.setNetSalary(dto.getNetSalary());
			//		SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
			//		Calendar cal = Calendar.getInstance();
			//		String crediteddate = f.format(cal.getTime());
			//		regeneratedSalary.setCreditedDate(crediteddate);
			//		DateTimeZone istTimeZone = DateTimeZone.forID("Asia/Kolkata");
			//		DateTime current = new DateTime(istTimeZone);
			//
			//		regeneratedSalary.setUpdatedWhen(new Timestamp(current.getMillis()));
			//		regeneratedSalary.setAbsentDays(existingSalary.getAbsentDays());
			//		regeneratedSalary.setHalfDay(existingSalary.getHalfDay());
			//		regeneratedSalary.setPaidLeave(existingSalary.getPaidLeave());
			//		regeneratedSalary.setUnpaidLeave(existingSalary.getUnpaidLeave());
			//		regeneratedSalary.setTotalWorkingDays(existingSalary.getTotalWorkingDays());


			DateTimeZone istTimeZone = DateTimeZone.forID("Asia/Kolkata");
			DateTime current = new DateTime(istTimeZone);
			dto.setActive(true);
			dto.setUpdatedWhen(new Timestamp(current.getMillis()));
			monthlySalaryDetailsRepo.save(dto);
			log.info("Monthly salary saved.");
			double amountPerDay = Math.round(dto.getGrossSalary() / dto.getTotalWorkingDays());
			ByteArrayOutputStream byteArrayOutputStream = DetailedSalarySlip.builder().build()
					.generateDetailedSalarySlipPDF(empid.toString(), name, dto.getTotalWorkingDays()!=null?dto.getTotalWorkingDays().intValue():0, dto.getPresentDays()!=null ? dto.getPresentDays().intValue():0, dto.getAbsentDays()!=null?dto.getAbsentDays().intValue():0,
							dto.getHalfDay()!= null ? dto.getHalfDay().intValue():0, empPayroll.get().getSalary().toString(),
							dto.getPaidLeave().toString(), date.toString(), empPayroll.get().getBankName(), empPayroll.get().getAccountNumber(), empPayroll.get().getDesignation(), empPayroll.get().getJoinDate(), dto.getAdhoc()!=null?dto.getAdhoc().intValue():0, "payPeriod",
							dto.getEmployeeESICAmount(), dto.getEmployeePFAmount().floatValue(), dto.getNetSalary(), dto.getGrossSalary(), dto.getBasic(), dto.getHouseRentAllowance() != null ? dto.getHouseRentAllowance() : 0.0, amountPerDay, dto.getUnpaidLeave(),
							dto.getAdjustment()!=null?dto.getAdjustment().intValue():0, dto.getMedicalInsurance()!=null? dto.getMedicalInsurance().intValue():0,  dto.getTds() != null ? dto.getTds().intValue() : 0);
			log.info("Payslip generated successfully ");
			mailService.sendEmail(byteArrayOutputStream, name, user.get().getEmail(),
					dto.getMonth() + " " + "year");
			log.info("Mail send successfully this email id=" + user.get().getEmail());
			return "Mail send successfully";
		} catch (Exception e) {
			log.info("Error while regenerating payslip ", e.getMessage());
			return null;
		}
	}
}