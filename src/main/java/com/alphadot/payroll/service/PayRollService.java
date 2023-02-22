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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import com.alphadot.payroll.model.LeaveTime;
import com.alphadot.payroll.model.PaySlip;
import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.model.User;
import com.alphadot.payroll.repository.LeaveTimeRepo;
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
	private LeaveTimeRepo leaveTimeRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private MessageSource messageSource;

	@Value("${holiday.republic}")
	private String republic;

	@Value("${holiday.holi}")
	private String holi;

	@Value("${holiday.rakhi}")
	private String rakhi;

	@Value("${holiday.independence}")
	private String independence;

	@Value("${holiday.gandhi}")
	private String gandhi;

	@Value("${holiday.dussehra}")
	private String dussehra;

	@Value("${holiday.diwali}")
	private String diwali;

	public PaySlip createPaySlip(int empId, String month, String year) throws ParseException, IOException {

		log.warn("inside method");

		List<String> li = new ArrayList<>();
		li.add(diwali);
		li.add(holi);
		li.add(rakhi);
		li.add(independence);
		li.add(gandhi);
		li.add(dussehra);
		li.add(republic);

		int yourWorkingDays = 0;
		int saturday = Util.SaturdyaValue;

		Map<String, Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		map.put(Util.January, 1);
		map.put(Util.February, 2);
		map.put(Util.March, 3);
		map.put(Util.April, 4);
		map.put(Util.May, 5);
		map.put(Util.June, 6);
		map.put(Util.July, 7);
		map.put(Util.August, 8);
		map.put(Util.September, 9);
		map.put(Util.October, 10);
		map.put(Util.November, 11);
		map.put(Util.December, 12);

		LocalDate currentdate = LocalDate.now();

		int leaves = 0;

		PaySlip paySlip = new PaySlip();

		Optional<User> user = userRepo.findById(empId);

		if (!user.isPresent()) {
			String message = messageSource.getMessage("api.error.data.not.found.id", null, Locale.ENGLISH);
			log.error(message = message + empId);
			throw new EntityNotFoundException(message);
		}

		List<TimeSheetModel> timeSheetModel = timeSheetRepo.search(empId, month.toUpperCase(), year);
		List<LeaveTime> leaveModel = leaveTimeRepo.findByEmpIdAndMonth(empId, month.toUpperCase());

		for (TimeSheetModel tm : timeSheetModel) {
			if (tm.getWorkingHour() != null && tm.getStatus().equalsIgnoreCase(Util.StatusPresent)) {
				yourWorkingDays++;
			}
		}

		leaves = leaveModel.size();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String monthDate = String.valueOf(map.get(month));

		String firstDayMonth = "01/" + monthDate + "/" + year;

		String lastDayOfMonth = (LocalDate.parse(firstDayMonth, DateTimeFormatter.ofPattern("dd/M/yyyy"))
				.with(TemporalAdjusters.lastDayOfMonth())).format(DateTimeFormatter.ofPattern("dd/M/yyyy"));

		int workDays = 0;
		int beforeHolidays = 0;
		int afterHolidays = 0;
		int diff = 0;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date startDate = formatter.parse(firstDayMonth);
		Date endDate = formatter.parse(lastDayOfMonth);

		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		Calendar end = Calendar.getInstance();
		end.setTime(endDate);

		List<String> lists = new ArrayList<>();

		LocalDate localDate = null;

		for (Date date = start.getTime(); start.before(end) || start.equals(end); start.add(Calendar.DATE,
				1), date = start.getTime()) {

			localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			lists.add(localDate.toString().strip());

			if (date.getDay() != 0) {
				workDays++;
			}

		}

		beforeHolidays = lists.size();
		lists.removeAll(li);
		afterHolidays = lists.size();

		diff = beforeHolidays - afterHolidays;
		workDays = workDays - diff;

		String path = Util.FolderPath + user.get().getFirstName() + user.get().getLastName() + "_" + month + ".pdf";
		log.warn("folder path set");

		int grossSalary = (int) user.get().getSalary();
		int totalWorkingDays = workDays - saturday;
		int amountPerDay = grossSalary / totalWorkingDays;
		int leavePerDay = leaves * amountPerDay;
		int netAmount = (yourWorkingDays * amountPerDay) - leavePerDay;
		extracted(yourWorkingDays, currentdate, leaves, user, dtf, firstDayMonth, lastDayOfMonth, path, grossSalary,
				totalWorkingDays, amountPerDay, leavePerDay, netAmount);

		extracted(empId, yourWorkingDays, currentdate, leaves, paySlip, user, dtf, firstDayMonth, lastDayOfMonth,
				grossSalary, totalWorkingDays, amountPerDay, leavePerDay, netAmount);

		return paySlip;

	}

	private void extracted(int yourWorkingDays, LocalDate currentdate, int leaves, Optional<User> user,
			DateTimeFormatter dtf, String firstDayMonth, String lastDayOfMonth, String path, int grossSalary,
			int totalWorkingDays, int amountPerDay, int leavePerDay, int netAmount)
			throws MalformedURLException, FileNotFoundException {
		ImageData datas = ImageDataFactory.create(Util.ImagePath);
		log.warn("image path set");
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
		employeeTable.addCell(
				new Cell().add(user.get().getFirstName() + " " + user.get().getLastName()).setBorder(Border.NO_BORDER));
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
	}

	private void extracted(int empId, int yourWorkingDays, LocalDate currentdate, int leaves, PaySlip paySlip,
			Optional<User> user, DateTimeFormatter dtf, String firstDayMonth, String lastDayOfMonth, int grossSalary,
			int totalWorkingDays, int amountPerDay, int leavePerDay, int netAmount) {
		paySlip.setEmpId(empId);
		paySlip.setJobTitle(user.get().getDesignation());
		paySlip.setAccountNumber(user.get().getAccountNumber());
		paySlip.setBankName(user.get().getBankName());
		paySlip.setName(user.get().getFirstName() + " " + user.get().getLastName());
		paySlip.setPresentDate(dtf.format(currentdate));
		paySlip.setMobileNo(user.get().getMobileNo());
		paySlip.setPayPeriods(firstDayMonth + " - " + lastDayOfMonth);
		paySlip.setYouWorkingDays(yourWorkingDays);
		paySlip.setTotalWorkingDays(totalWorkingDays);
		paySlip.setNumberOfLeavesTaken(leaves);
		paySlip.setAmountDeductedForLeaves(leavePerDay);
		paySlip.setAmountPayablePerDay(amountPerDay);
		paySlip.setGrossSalary(grossSalary);
		paySlip.setNetAmountPayable(netAmount);
	}

}