package com.alphadot.payroll.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.alphadot.payroll.model.ImageModel;
import com.alphadot.payroll.model.PaySlip;
import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.model.User;
import com.alphadot.payroll.repository.ImageRepo;
import com.alphadot.payroll.repository.TimeSheetRepo;
import com.alphadot.payroll.repository.UserRepo;
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

@Service
public class PayRollService {

	private static final Logger log = LogManager.getLogger(PayRollService.class);
	@Autowired
	private JavaMailSender javaMailSender;

	@Value("${spring.mail.username}")
	private String sender;
	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private ImageRepo imgRepo;

	@Value("${holiday}")
	private String[] holiday;

	@Value("${Excel_Path}")
	private String excelPath;

	public PaySlip createPaySlip(int empId, String month, String year,int adhoc)
			throws ParseException, IOException, SQLException {
		log.info("inside method");
		String monthYear = month + " " + year;
		PaySlip paySlip = new PaySlip();

		List<String> holidays = Arrays.asList(holiday);
		List<String> lists = new ArrayList<>();

		int yourWorkingDays = 0, leaves = 0, workDays = 0, saturday = Util.SaturdyaValue;

		LocalDate currentdate = LocalDate.now();

		SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
		SimpleDateFormat outputFormat = new SimpleDateFormat("MM"); // 01-12

		Calendar cal = Calendar.getInstance();
		cal.setTime(inputFormat.parse(month));

		Optional<User> user = Optional.ofNullable(userRepo.findById(empId)
				.orElseThrow(() -> new EntityNotFoundException("employee not found :" + empId)));
		String name = user.get().getFirstName() + " " + user.get().getLastName();
		List<TimeSheetModel> timeSheetModel = timeSheetRepo.search(empId, month.toUpperCase(), year);
		yourWorkingDays = timeSheetModel.stream()
				.filter(x -> x.getWorkingHour() != null && x.getStatus().equalsIgnoreCase(Util.StatusPresent))
				.collect(Collectors.toList()).size();
		leaves = timeSheetModel.stream()
				.filter(x -> x.getWorkingHour() == null && (x.getCheckIn()==null && x.getStatus().equalsIgnoreCase("Leave")))
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

		log.info("folder path set");

		float grossSalary = (int) user.get().getSalary();
		int totalWorkingDays = workDays - saturday;
		float amountPerDay = grossSalary / totalWorkingDays;
		float leavePerDay = leaves * amountPerDay;
		float netAmount = (yourWorkingDays * amountPerDay);
              netAmount=  netAmount +adhoc;
		paySlip = new PaySlip(empId, name, user.get().getDesignation(),
				dtf.format(currentdate), user.get().getBankName(), user.get().getAccountNumber(),
				firstDayMonth + " - " + lastDayOfMonth, yourWorkingDays, totalWorkingDays, leaves, leavePerDay,
				 grossSalary, netAmount,adhoc);
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
		employeeTable.addCell(new Cell().add(user.get().getBankName()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.JobTitle).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(user.get().getDesignation()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.AccountNumber).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(user.get().getAccountNumber()).setBorder(Border.NO_BORDER));

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
		itemInfo.addCell(new Cell().add(Util.AmountDeductedForLeaves));
		itemInfo.addCell(new Cell().add(String.valueOf(leavePerDay)));

		itemInfo.addCell(new Cell().add(Util.GrossSalary));
		itemInfo.addCell(new Cell().add(String.valueOf(grossSalary)));
		itemInfo.addCell(new Cell().add(Util.NetAmountPayable));
		itemInfo.addCell(new Cell().add(String.valueOf(netAmount)));
		document.add(alpha);

		document.add(table);
		document.add(new Paragraph("\n"));
		document.add(employeeTable);
		document.add(itemInfo);
		document.add(new Paragraph("\n(Authorised Singnatory)").setTextAlignment(TextAlignment.RIGHT));
		document.close();
		log.warn("Successfully");

		sendEmail(baos, name, user.get().getEmail(), monthYear);
		return paySlip;
	}

	// Excel Pay Slip

	public void generatePaySlip() throws IOException, ParseException {
		String empId = "", name = "", workingDays = "", present = "", leave = "", halfDay = "", salary = "",
				paidLeave = "", bankName = "", accountNumber = "", gmail = "", designation = "";

		int adhoc = 0;

		SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
		SimpleDateFormat outputFormat = new SimpleDateFormat("MM");
		NumberFormat format = NumberFormat.getInstance();
		String projDir = System.getProperty("user.dir");
		XSSFWorkbook workbook = new XSSFWorkbook(excelPath);
		DataFormatter dataFormatter = new DataFormatter();
		XSSFSheet sheet = workbook.getSheet("Sheet1");
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
		for (int i = 2; i <= 50; i++) {
			try {
				XSSFRow row = sheet.getRow(i);

				try {
					empId = dataFormatter.formatCellValue(row.getCell(0));
					name = dataFormatter.formatCellValue(row.getCell(1));
					workingDays = dataFormatter.formatCellValue(row.getCell(2));
					present = dataFormatter.formatCellValue(row.getCell(3));
					leave = dataFormatter.formatCellValue(row.getCell(4));
					halfDay = dataFormatter.formatCellValue(row.getCell(5));
					salary = dataFormatter.formatCellValue(row.getCell(6));
					paidLeave = dataFormatter.formatCellValue(row.getCell(7));
					bankName = dataFormatter.formatCellValue(row.getCell(8));
					accountNumber = format.format(row.getCell(9).getNumericCellValue()).replace(",", "");
					designation = dataFormatter.formatCellValue(row.getCell(10));
					gmail = dataFormatter.formatCellValue(row.getCell(11));
					adhoc = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(12)));

					ByteArrayOutputStream baos = createPdf(empId, name, workingDays, present, leave, halfDay, salary,
							paidLeave, date, bankName, accountNumber, designation, adhoc, payPeriod);
					sendEmail(baos, name, gmail, monthYear);
				} catch (Exception e) {
					continue;
				}
			} catch (Exception e) {
				break;
			}
		}

	}

	public ByteArrayOutputStream createPdf(String empId, String name, String totalworkingDays, String present,
			String leave, String halfDay, String salary, String paidLeave, String date, String bankName,
			String accountNumber, String designation, int adhoc, String payPeriod) throws SQLException, IOException {

		float grossSalary = Float.valueOf(salary);
		int totalWorkingDays = Integer.parseInt(totalworkingDays);
		int leaves = Integer.parseInt(leave) - Integer.parseInt(paidLeave);
		int yourWorkingDays = Integer.parseInt(present) + Integer.parseInt(paidLeave);

		float amountPerDay = grossSalary / totalWorkingDays;

		float HalfDays = Integer.parseInt(halfDay) * amountPerDay / 2;
		float leavePerDay = leaves * amountPerDay;
		float netAmount = (yourWorkingDays * amountPerDay) - HalfDays;

		netAmount = netAmount + adhoc;
		if (netAmount < 0) {
			netAmount = 0;
			adhoc = 0;
		}

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
		float colWidth[] = { 150, 150, 100, 100 };
		Table employeeTable = new Table(colWidth);
		employeeTable.addCell(new Cell(0, 4).add(Util.EmployeeInformation).setBold());
		employeeTable.addCell(new Cell().add(Util.EmployeeNumber).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(empId).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.Date).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(date).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.Name).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(name).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.BankName).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(bankName).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.JobTitle).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(designation).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.AccountNumber).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(accountNumber).setBorder(Border.NO_BORDER));
		Table itemInfo = new Table(columnWidth);
		itemInfo.addCell(new Cell().add(Util.PayPeriods));
		itemInfo.addCell(new Cell().add(payPeriod));
		itemInfo.addCell(new Cell().add(Util.YourWorkingDays));
		itemInfo.addCell(new Cell().add(present));
		itemInfo.addCell(new Cell().add(Util.TotalWorkingDays));
		itemInfo.addCell(new Cell().add(totalworkingDays));
		itemInfo.addCell(new Cell().add(Util.NumberOfLeavesTaken));
		itemInfo.addCell(new Cell().add(leave));
		itemInfo.addCell(new Cell().add("Paid Leave"));
		itemInfo.addCell(new Cell().add(paidLeave));
		itemInfo.addCell(new Cell().add(Util.AmountDeductedForLeaves));
		itemInfo.addCell(new Cell().add(String.format("%.2f", leavePerDay)));
		itemInfo.addCell(new Cell().add(Util.Adhoc));
		itemInfo.addCell(new Cell().add(String.valueOf(adhoc)));
		itemInfo.addCell(new Cell().add(Util.GrossSalary));
		itemInfo.addCell(new Cell().add(String.valueOf(salary)));
		itemInfo.addCell(new Cell().add(Util.NetAmountPayable));
		itemInfo.addCell(new Cell().add(String.format("%.2f", netAmount)));
		document.add(alpha);
		document.add(table);
		document.add(new Paragraph("\n"));
		document.add(employeeTable);
		document.add(itemInfo);
		document.add(new Paragraph("\n(Authorised Signatory )").setTextAlignment(TextAlignment.RIGHT));
		document.close();

		log.info("Successfully");
		return baos;
	}

	public void sendEmail(ByteArrayOutputStream baos, String name, String gmail, String monthYear) {
		String massage = Util.msg.replace("[Name]", name).replace("[Your Name]", "AlphaDot Technologies")
				.replace("[Month, Year]", monthYear);

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper;

		try {
			MimeBodyPart attachmentBodyPart = new MimeBodyPart();
			DataSource source = new ByteArrayDataSource(baos.toByteArray(), "application/octet-stream");
			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setFrom(sender);
			mimeMessageHelper.setTo(gmail);
			mimeMessageHelper.setText(massage);
			mimeMessageHelper.setSubject("Salary Slip" + "-" + monthYear);
			mimeMessageHelper.addAttachment(name + ".pdf", source);

			javaMailSender.send(mimeMessage);

			log.info("Mail send Successfully");
		}

		catch (MessagingException e) {
			log.info("Error");

		}

	}

}