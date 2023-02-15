package com.alphadot.payroll.service;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import com.alphadot.payroll.model.Employee;
import com.alphadot.payroll.model.LeaveTime;
import com.alphadot.payroll.model.PaySlip;
import com.alphadot.payroll.model.SalaryModel;
import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.model.User;
import com.alphadot.payroll.repository.EmployeeRepo;
import com.alphadot.payroll.repository.LeaveTimeRepo;
import com.alphadot.payroll.repository.SalaryRepo;
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

	private static final Logger log=LogManager.getLogger(PayRollService.class);

	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Autowired
	private LeaveTimeRepo leaveTimeRepo;
	
	@Autowired
	private SalaryRepo salaryRepo;
	
	@Autowired
	private UserRepo userRepo;
	

	public PaySlip createPaySlip(int empId, String month) throws FileNotFoundException, MalformedURLException {
		  log.warn("inside method");
		int yourWorkingDays = 0;
		SalaryModel salaryModel = new SalaryModel();
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
		String year = String.valueOf(currentdate.getYear());
		int leaves = 0;
		

			User user = new User();
			PaySlip paySlip = new PaySlip();

			user = userRepo.findById(empId);
			List<TimeSheetModel> timeSheetModel = timeSheetRepo.search(empId, month.toUpperCase(), year);
			List<LeaveTime> leaveModel = leaveTimeRepo.findByEmpIdAndMonth(empId, month.toUpperCase());

			yourWorkingDays = timeSheetModel.size();
			leaves = leaveModel.size();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			String monthDate = String.valueOf(map.get(month));

			String firstDayMonth = "01/" + monthDate + "/" + Util.Year;

			String lastDayOfMonth = (LocalDate.parse(firstDayMonth, DateTimeFormatter.ofPattern("dd/M/yyyy"))
					.with(TemporalAdjusters.lastDayOfMonth())).format(DateTimeFormatter.ofPattern("dd/M/yyyy"));

			String path = Util.FolderPath + user.getFirstName() + user.getLastName() + "_"
					+ month + ".pdf";
			  log.warn("folder path set");

			int grossSalary = (int)user.getSalary();
			
			int totalWorkingDays = leaves + yourWorkingDays;
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
					.setVerticalAlignment(VerticalAlignment.MIDDLE).setMarginTop(30f).setMarginBottom(30f)
					.setFontSize(30f).setBorder(Border.NO_BORDER));
			table.addCell(new Cell().add(Util.ADDRESS).setTextAlignment(TextAlignment.RIGHT).setMarginTop(30f)
					.setMarginBottom(30f).setBorder(Border.NO_BORDER).setMarginRight(10f));
			float colWidth[] = { 150, 150, 100, 100 };
			Table employeeTable = new Table(colWidth);
			employeeTable.addCell(new Cell(0, 4).add(Util.EmployeeInformation).setBold());
			employeeTable.addCell(new Cell().add(Util.EmployeeInformation).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(String.valueOf(user.getId())).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(Util.Date).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(dtf.format(currentdate)).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(Util.Name).setBorder(Border.NO_BORDER));
			employeeTable
					.addCell(new Cell().add(user.getFirstName() + " " + user.getLastName()).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(Util.BankName).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(user.getBankName()).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(Util.JobTitle).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(user.getDesignation()).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(Util.AccountNumber).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(user.getAccountNumber()).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell().add(Util.MobileNo).setBorder(Border.NO_BORDER));
			employeeTable.addCell(new Cell(0, 4).add(String.valueOf(user.getMobileNo())).setBorder(Border.NO_BORDER));
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
			itemInfo.addCell(new Cell().add(String.valueOf((grossSalary / totalWorkingDays) * leaves)));
			itemInfo.addCell(new Cell().add(Util.AmountPayablePerDay));
			itemInfo.addCell(new Cell().add(String.valueOf(grossSalary / totalWorkingDays)));
			itemInfo.addCell(new Cell().add(Util.GrossSalary));
			itemInfo.addCell(new Cell().add(String.valueOf(grossSalary)));
			itemInfo.addCell(new Cell().add(Util.NetAmountPayable));
			itemInfo.addCell(new Cell().add(String.valueOf((grossSalary - (grossSalary / totalWorkingDays) * leaves))));
			document.add(alpha);
			document.add(table);
			document.add(new Paragraph("\n"));
			document.add(employeeTable);
			document.add(itemInfo);
			document.add(new Paragraph("\n(Authorised Singnatory)").setTextAlignment(TextAlignment.RIGHT));
			document.close();
			  log.warn("Successfully");
			
			  paySlip.setEmpId(empId);
			  paySlip.setJobTitle(user.getDesignation());
			  paySlip.setAccountNumber(user.getAccountNumber());
			  paySlip.setBankName(user.getBankName());
			  paySlip.setName(user.getFirstName()+" "+user.getLastName());
			  paySlip.setPresentDate(dtf.format(currentdate));
			  paySlip.setMobileNo(user.getMobileNo());
			  paySlip.setPayPeriods(firstDayMonth + " - " + lastDayOfMonth);
			  paySlip.setYouWorkingDays(yourWorkingDays);
			  paySlip.setTotalWorkingDays(totalWorkingDays);
			  paySlip.setNumberOfLeavesTaken(leaves);
			  paySlip.setAmountDeductedForLeaves((grossSalary / totalWorkingDays) * leaves);
			  paySlip.setAmountPayablePerDay(grossSalary / totalWorkingDays);
			  paySlip.setGrossSalary(grossSalary);
			  paySlip.setNetAmountPayable((grossSalary - (grossSalary / totalWorkingDays) * leaves));
			  
			  salaryModel.setEmpId(empId);
			  salaryModel.setMonth(month);
			  salaryModel.setLeaveCounts(leaves);
			  salaryModel.setName(user.getFirstName()+" "+user.getLastName());
			  salaryModel.setWorkedDays(yourWorkingDays);
			  salaryModel.setTotalWorkingDays(totalWorkingDays);
			  salaryModel.setYear(year);
			  salaryRepo.save(salaryModel);
			return paySlip;
		

	}

}