package com.adt.payroll.service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.adt.payroll.exception.NoDataFoundException;
import com.adt.payroll.model.EmpPayrollDetails;
import com.adt.payroll.model.ImageModel;
import com.adt.payroll.model.PayRecord;
import com.adt.payroll.model.PaySlip;
import com.adt.payroll.model.SalaryModel;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.EmpPayrollDetailsRepo;
import com.adt.payroll.repository.ImageRepo;
import com.adt.payroll.repository.PayRecordRepo;
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

import jakarta.persistence.EntityNotFoundException;

@Service
public class PayRollServiceImpl implements PayRollService {

	private static final Logger log = LogManager.getLogger(PayRollServiceImpl.class);

	@Autowired
	private JavaMailSender javaMailSender;

//    @Value("${spring.mail.username}")
//    private String sender;

	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Autowired
	private PayRecordRepo payRecordRepo;
	@Autowired
	private TableDataExtractor dataExtractor;

	@Autowired
	private UserRepo userRepo;
	
	@Autowired 
	EmpPayrollDetailsRepo 	empPayrollDetailsRepo;

	@Autowired
	private ImageRepo imgRepo;

	@Value("${holiday}")
	private String[] holiday;

	@Autowired
	Util util;

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
		
		Optional<User> user = Optional.ofNullable(userRepo.findById(empId)
				.orElseThrow(() -> new NoDataFoundException("employee not found :" + empId)));
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
				empDetails.get().getBankName(), empDetails.get().getAccountNumber(), firstDayMonth + " - " + lastDayOfMonth,
				yourWorkingDays, totalWorkingDays, leaves, leavePerDay, grossSalary, netAmount, adhoc);
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

	public String generatePaySlip(MultipartFile file) throws IOException, ParseException {
		String empId = "", name = "", salary = "", esic = "", pf = "", paidLeave = "", bankName = "",
				accountNumber = "", gmail = "", designation = "", submitDate = "", status = "", employee_id = "",
				joiningDate = "";
		String sheetName = "";
	    int adjustment = 0,tds=0, adhoc1 = 0, medicalInsurance = 0, adhoc3 = 0, workingDays = 0, present = 0, leave = 0, halfDay = 0, limit = 30;
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

		String monthYear = String.valueOf(earlier.getMonth() + " " + earlier.getYear());
		String firstDayMonth = "01/" + monthDate + "/" + +earlier.getYear();
		String lastDayOfMonth = (LocalDate.parse(firstDayMonth, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
				.with(TemporalAdjusters.lastDayOfMonth())).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		String payPeriod = firstDayMonth + " - " + lastDayOfMonth;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String date = dtf.format(currentdate);
		String sql = "select * from employee_schema.employee_expenses";
		List<Map<String, Object>> tableData = dataExtractor.extractDataFromTable(sql);
		List<User> employee = userRepo.findAll();
		int workingDay = util.getWorkingDays();
		for (int i = 2; i <= sheet.getLastRowNum(); i++) {
			System.out.println(sheet.getLastRowNum());
			try {
				XSSFRow row = sheet.getRow(i);
				try {
					 empId = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.EmployeeNumber)));
	                    name = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Name)));
	                    workingDays = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.TotalWorkingDays))));
	                    present = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.YourWorkingDays))));
	                    leave = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Leave))));
	                    halfDay = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.HalfDay))));
	                    salary = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.salary)));
	                    paidLeave = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.PaidLeave)));
	                    bankName = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.BankName)));
	                    accountNumber = format.format(row.getCell(excelColumnName.get(Util.AccountNumber)).getNumericCellValue()).replace(",", "");
	                    designation = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.DESIGNATION)));
	                    gmail = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Gmail)));
	                    joiningDate = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.JoiningDate)));
	        			bankName = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.BankName)));
					 try {
	                        adjustment= Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.ADJUSTMENT))));
	                    } catch (NumberFormatException e) {
	                        adjustment = 0;
	                    }
	                    try {
	                        tds= Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.TDS))));
	                    } catch (NumberFormatException e) {
	                        tds = 0;
	                    }
	                    try {
	                        medicalInsurance= Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.MEDICAL_INSURANCE))));
	                    } catch (NumberFormatException e) {
	                        medicalInsurance = 0;
	                    }

	                    try {
	                        adhoc1 = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc1))));
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
					
					  if (checkEmpDetails(empId,gmail, accountNumber,employee, fName, lName)) {
					  mailService.sendEmail(name);
					  continue;
					  
					  }
					 
					if (isNotNull(empId, name, workingDays, present, date, bankName, accountNumber, designation,
							joiningDate, leave, halfDay, adhoc1,  salary, workingDay, paidLeave)) {
						baos = createPdf(empId, name, workingDays, present, leave, halfDay, salary,
	                            paidLeave, date, bankName, accountNumber, designation, joiningDate, adhoc1, payPeriod, esic, pf,adjustment,medicalInsurance,tds);
						
						  } else {
						  
						  mailService.sendEmail(name);
						  continue;
						  }
						 

//                    sendEmail(baos, name, gmail, monthYear);

					mailService.sendEmail(baos, name, gmail, monthYear);

				} catch (Exception e) {
					mailService.sendEmail(name);
					continue;
				}
			} catch (Exception e) {
				break;
			}
		}
		return "Mail Send Successfully";
	}

	 public ByteArrayOutputStream createPdf(String empId, String name, int totalWorkingDays, int present,
             int leave, int halfDay, String salary, String paidLeave, String date, String bankName,
             String accountNumber, String designation, String joiningDate, int adhoc1, String payPeriod, String esic, String pf,int adjustment,int medicalInsurance,int tds) throws IOException, DocumentException {

		float pfAmount = 0;
        double grossSalary = Double.parseDouble(salary);

        if (esic.equals("Yes") && pf.equalsIgnoreCase("Yes")) {
            grossSalary = Math.round(grossSalary - ((grossSalary / 2) * 0.13) - grossSalary * 0.04 + (grossSalary * 0.01617));
        } else if (esic.equals("No") && pf.equalsIgnoreCase("Yes")) {
           
            grossSalary = Math.round(grossSalary - ((grossSalary / 2) * 0.13) + (grossSalary * 0.01617));
        }
        float esicAmount = 0;
        double basic = Math.round(grossSalary / 2);

        double hra = Math.round(grossSalary / 2);
        int yourWorkingDays = present + Integer.parseInt(paidLeave);

        double amountPerDay = grossSalary / totalWorkingDays;
        double unpaidLeave = totalWorkingDays - present;
         unpaidLeave -=Integer.parseInt(paidLeave);
         unpaidLeave *=amountPerDay;

        double HalfDays = halfDay * amountPerDay / 2;

        double netAmount = Math.round((yourWorkingDays * amountPerDay) - HalfDays);

        netAmount = Math.round(netAmount + adhoc1);
        if (netAmount < 0) {
            netAmount = 0;
            adhoc1 = 0;
        }

        if (esic.equalsIgnoreCase("yes") && netAmount != 0) {
            esicAmount = (float) (Math.round(grossSalary * (0.0075)));

        }

        if (pf.equalsIgnoreCase("yes") && netAmount != 0) {
            pfAmount = (float) (Math.round(basic * 0.120));

        }
        netAmount -= esicAmount;
        netAmount -= pfAmount;
        netAmount = Math.round(netAmount);
        netAmount -= medicalInsurance;
        netAmount -= adjustment;

        ByteArrayOutputStream byteArrayOutputStream =  DetailedSalarySlip.builder().build().generateDetailedSalarySlipPDF( empId,  name,  totalWorkingDays,  present,
                leave,  halfDay,  salary,  paidLeave,  date,  bankName,
                accountNumber,  designation,  joiningDate,  adhoc1,  payPeriod,  esicAmount,  pfAmount,netAmount,grossSalary,basic,hra,amountPerDay,unpaidLeave,adjustment,medicalInsurance,tds);
        return byteArrayOutputStream;
	}

//    public void sendEmail(ByteArrayOutputStream baos, String name, String gmail, String monthYear) {
//        String massage = Util.msg.replace("[Name]", name).replace("[Your Name]", "AlphaDot Technologies")
//                .replace("[Month, Year]", monthYear);
//
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//        MimeMessageHelper mimeMessageHelper;
//
//        try {
//
//            DataSource source = new ByteArrayDataSource(baos.toByteArray(), "application/octet-stream");
//            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
//            mimeMessageHelper.setFrom(sender);
//            mimeMessageHelper.setTo(gmail);
//            mimeMessageHelper.setText(massage);
//            mimeMessageHelper.setSubject("Salary Slip" + "-" + monthYear);
//            mimeMessageHelper.addAttachment(name + ".pdf", source);
//
//            javaMailSender.send(mimeMessage);
//
//            log.info("Mail send Successfully");
//        } catch (MessagingException e) {
//            log.info("Error");
//
//        }
//
//    }

	@Override
	public byte[] viewPay(SalaryModel salaryModel, String month, String year)
			throws ParseException, UnsupportedEncodingException {
		log.info("inside method");
		int empId = salaryModel.getEmpId();
		List<PayRecord> payRecordList = payRecordRepo.findByEmpId(empId);

		for (PayRecord payRecord : payRecordList) {
			if (payRecord != null) {
				if (payRecord.getEmpId() == empId && payRecord.getMonth().equalsIgnoreCase(month)
						&& payRecord.getYear().equalsIgnoreCase(year))
					return payRecord.getPdf();
			}
		}

		String submitDate = "", status = "", employee_id = "";
		String monthYear = month + " " + year;
		int yourWorkingDays = 0, leaves = 0, workDays = 0, saturday = Util.SaturdyaValue, adhoc = 0;
		LocalDate currentdate = LocalDate.now();

		String sql = "select * from employee_schema.employee_expenses";
		List<Map<String, Object>> tableData = dataExtractor.extractDataFromTable(sql);

		PayRecord payRecord = new PayRecord();
		payRecord.setEmpId(empId);
		payRecord.setMonth(month);
		payRecord.setEmpName(salaryModel.getEmpName());
		payRecord.setYear(year);

		List<String> holidays = Arrays.asList(holiday);
		List<String> lists = new ArrayList<>();

		SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
		SimpleDateFormat outputFormat = new SimpleDateFormat("MM"); // 01-12

		Calendar cal = Calendar.getInstance();
		cal.setTime(inputFormat.parse(month));

		Optional<User> user = Optional.ofNullable(userRepo.findById(empId)
				.orElseThrow(() -> new EntityNotFoundException("employee not found :" + empId)));
		String name = salaryModel.getEmpName();
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
			submitDate = String.valueOf(expense.get("payment_date")).substring(3, 5);
			status = String.valueOf(expense.get("status"));
			employee_id = String.valueOf(expense.get("employee_id"));
			if (submitDate.equals(monthDate) && status.equals("Accepted")
					&& employee_id.equalsIgnoreCase(String.valueOf(empId))) {
				adhoc += Integer.parseInt(String.valueOf(expense.get("expense_amount")));
			}
		}

		float grossSalary = salaryModel.getSalary();
		int totalWorkingDays = workDays - saturday;
		float amountPerDay = grossSalary / totalWorkingDays;
		float leavePerDay = leaves * amountPerDay;
		float netAmount = (yourWorkingDays * amountPerDay);
		netAmount += adhoc;

		ImageModel img = new ImageModel();

		ImageData datas = ImageDataFactory.create(imgRepo.search());
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
		float colWidth[] = { 125, 150, 125, 100 };
		Table employeeTable = new Table(colWidth);
		employeeTable.addCell(new Cell(0, 4).add(
				Util.EmployeeInformation + "                                                                          "
						+ "Date : " + dtf.format(currentdate))
				.setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.EmployeeNumber).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(String.valueOf(empId)).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.JoiningDate).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(salaryModel.getJoinDate()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.Name).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(salaryModel.getEmpName()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.BankName).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(salaryModel.getBankName()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.JobTitle).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(salaryModel.getRole()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.AccountNumber).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(salaryModel.getAccountNumber()).setBorder(Border.NO_BORDER));

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
		payRecord.setPdf(baos.toByteArray());
		payRecordRepo.save(payRecord);

		return baos.toByteArray();
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

	public boolean checkEmpDetails(String empId,String gmail,String accountNumber, List<User> employees, String fname,
			String lName ){
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
				if (empDetails.getAccountNumber().equalsIgnoreCase(accountNumber)) {
					flag = false;
				}
				return flag;
			}
		}

		return flag;
	}

	public boolean isNotNull(String empId, String name, Integer workingDays, Integer present, String date,
			String bankName, String accountNumber, String designation, String joiningDate, Integer leave,
			Integer halfDay, Integer adhoc1,  String salary, int workingDay,
			String paidLeave) {
		int intSalary = Integer.parseInt(salary);
		int intpaidLeave = Integer.parseInt(paidLeave);
		if (empId.isEmpty() || empId == null || name.isEmpty() || name == null || workingDays <= 0
				|| present > workingDays || leave < 0 || halfDay < 0 || workingDays != workingDay || bankName.isEmpty()
				|| accountNumber.isEmpty() || designation.isEmpty() || designation == null || joiningDate.isEmpty()
				|| joiningDate == null || (present > 0 && (salary == null || intSalary == 0))
				|| (present == 0 && (intSalary != 0 || intSalary < 0))
				||(workingDays!=present+leave+intpaidLeave) || (intpaidLeave < 0)
				|| (workingDays == present && (leave > 0))

		) {
			return false;

		}
		return true;
	}

}