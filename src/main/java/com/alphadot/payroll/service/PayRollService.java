package com.alphadot.payroll.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
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
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.PaySlip;
import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.model.User;
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
	private TimeSheetRepo timeSheetRepo;

	@Autowired
	private UserRepo userRepo;

	@Value("${holiday}")
	private String[] holiday;

	public PaySlip createPaySlip(int empId, String month, String year) throws ParseException, IOException {
		log.info("inside method");

		 PaySlip paySlip = new PaySlip();		

		List<String> holidays = Arrays.asList(holiday);
		List<String> lists = new ArrayList<>();

		int yourWorkingDays = 0, leaves = 0, workDays = 0, saturday = Util.SaturdyaValue;

		LocalDate currentdate = LocalDate.now();

		SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
		SimpleDateFormat outputFormat = new SimpleDateFormat("MM"); // 01-12

		Calendar cal = Calendar.getInstance();
		cal.setTime(inputFormat.parse(month));

		Optional<User> user = Optional.ofNullable(userRepo.findById(empId).orElseThrow(()-> new EntityNotFoundException("employee not found :"+empId)));
		List<TimeSheetModel> timeSheetModel = timeSheetRepo.search(empId, month.toUpperCase(), year);		
		yourWorkingDays = timeSheetModel.stream().filter(x -> x.getWorkingHour()!=null && x.getStatus().equalsIgnoreCase(Util.StatusPresent)).collect(Collectors.toList()).size();
		leaves = timeSheetModel.stream().filter(x -> x.getWorkingHour()==null && x.getStatus().equalsIgnoreCase("Leave")).collect(Collectors.toList()).size();
		
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
		String path = Util.FolderPath + user.get().getFirstName() + user.get().getLastName() + "_" + month + ".pdf";
		log.info("folder path set");

		float grossSalary = (int) user.get().getSalary();
		int totalWorkingDays = workDays - saturday;
		float amountPerDay = grossSalary / totalWorkingDays;
		float leavePerDay = leaves * amountPerDay;
		float netAmount = (yourWorkingDays * amountPerDay);

		paySlip = new PaySlip(empId, user.get().getFirstName() + " " + user.get().getLastName(),
				user.get().getDesignation(), user.get().getMobileNo(), dtf.format(currentdate),
				user.get().getBankName(), user.get().getAccountNumber(), firstDayMonth + " - " + lastDayOfMonth,
				yourWorkingDays, totalWorkingDays, leaves, leavePerDay, amountPerDay, grossSalary, netAmount);

		ImageData datas = ImageDataFactory.create(Util.ImagePath);
		log.info("image path set");
		Image alpha = new Image(datas);
		PdfWriter pdfWriter = new PdfWriter(path);
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
		employeeTable.addCell(new Cell().add(user.get().getFirstName() + " " + user.get().getLastName()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.BankName).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(user.get().getBankName()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.JobTitle).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(user.get().getDesignation()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.AccountNumber).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(user.get().getAccountNumber()).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell().add(Util.MobileNo).setBorder(Border.NO_BORDER));
		employeeTable.addCell(new Cell(0, 4).add(String.valueOf(user.get().getMobileNo())).setBorder(Border.NO_BORDER));
		Table itemInfo = new Table(columnWidth);
		itemInfo.addCell(new Cell().add(Util.PayPeriods));
		itemInfo.addCell(new Cell().add(firstDayMonth + " - " + lastDayOfMonth));
		itemInfo.addCell(new Cell().add(Util.YourWorkingDays));
		itemInfo.addCell(new Cell().add(String.valueOf(yourWorkingDays)));
		itemInfo.addCell(new Cell().add(Util.TotalWorkingDays));
		itemInfo.addCell(new Cell().add(String.valueOf(totalWorkingDays)));
		itemInfo.addCell(new Cell().add(Util.NumberOfLeavesTaken));
		itemInfo.addCell(new Cell().add(String.valueOf(leaves)));
		itemInfo.addCell(new Cell().add(Util.AmountDeductedForLeaves));
		itemInfo.addCell(new Cell().add(String.valueOf(leavePerDay)));
		itemInfo.addCell(new Cell().add(Util.AmountPayablePerDay));
		itemInfo.addCell(new Cell().add(String.valueOf(amountPerDay)));
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
		return paySlip;
	}
	
	
	//Excel Pay Slip
	
	public void generatePaySlip() throws IOException {
		String empId = "";
		String name = "";
		String workingDays = "";
		String present = "";
		String leave = "";
		String halfDay = "";
		String salary = "";
		String paidLeave = "";
		String bankName = "";
		String accountNumber = "";
		String designation = "";
	
		
		String path = "";
		String projDir = System.getProperty("user.dir");
		XSSFWorkbook workbook = new XSSFWorkbook(Util.ExcelPath);
		DataFormatter dataFormatter = new DataFormatter();
		XSSFSheet sheet = workbook.getSheet("Sheet1");
		LocalDate currentdate = LocalDate.now();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String date = dtf.format(currentdate);
		for (int i = 2; i <= 50; i++) {
			try {
				XSSFRow row = sheet.getRow(i);

				try {
					empId = dataFormatter.formatCellValue(row.getCell(0));
					name = dataFormatter.formatCellValue(row.getCell(1));
					path = Util.FolderPath + name + ".pdf";
					workingDays = dataFormatter.formatCellValue(row.getCell(2));
					present = dataFormatter.formatCellValue(row.getCell(3));
					leave = dataFormatter.formatCellValue(row.getCell(4));
					halfDay = dataFormatter.formatCellValue(row.getCell(5));
					salary = dataFormatter.formatCellValue(row.getCell(6));
					paidLeave = dataFormatter.formatCellValue(row.getCell(7));
					bankName = dataFormatter.formatCellValue(row.getCell(8));
					accountNumber = dataFormatter.formatCellValue(row.getCell(9));
					designation = dataFormatter.formatCellValue(row.getCell(10));
					
					createPdf(empId,name,workingDays,present,leave,halfDay,salary,paidLeave,date,path,bankName,accountNumber,designation);

				} catch (Exception e) {
					continue;
				}

			} catch (Exception e) {
				break;
			}

		}
         
		
	}
	
	public static void createPdf(String empId,String name,String totalworkingDays,String present,String leave,String halfDay,String salary,String paidLeave,String date,String path,String bankName,String accountNumber,String designation) throws MalformedURLException, FileNotFoundException {
	
		float grossSalary = Float.valueOf(salary);
		int totalWorkingDays = Integer.parseInt(totalworkingDays);
		int leaves = Integer.parseInt(leave) - Integer.parseInt(paidLeave);
		int yourWorkingDays = Integer.parseInt(present)+Integer.parseInt(paidLeave) ;
		
		float amountPerDay = grossSalary / totalWorkingDays;
		float HalfDays = Integer.parseInt(halfDay) * amountPerDay/2;
		float leavePerDay = leaves * amountPerDay;
		float netAmount = (yourWorkingDays * amountPerDay) - HalfDays;

		ImageData datas = ImageDataFactory.create(Util.ImagePath);
		log.info("image path set");
		Image alpha = new Image(datas);
		PdfWriter pdfWriter = new PdfWriter(path);
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
		itemInfo.addCell(new Cell().add("01/02/2023 - 28/02/2023"));
		itemInfo.addCell(new Cell().add(Util.YourWorkingDays));
		itemInfo.addCell(new Cell().add(present));
		itemInfo.addCell(new Cell().add(Util.TotalWorkingDays));
		itemInfo.addCell(new Cell().add(totalworkingDays));
		itemInfo.addCell(new Cell().add(Util.NumberOfLeavesTaken));
		itemInfo.addCell(new Cell().add(leave));
		itemInfo.addCell(new Cell().add("Paid Leave"));
		itemInfo.addCell(new Cell().add(paidLeave));
		itemInfo.addCell(new Cell().add(Util.AmountDeductedForLeaves));
		itemInfo.addCell(new Cell().add(String.valueOf(leavePerDay)));
		
		itemInfo.addCell(new Cell().add(Util.GrossSalary));
		itemInfo.addCell(new Cell().add(String.valueOf(salary)));
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
	}
	 
	
	
}