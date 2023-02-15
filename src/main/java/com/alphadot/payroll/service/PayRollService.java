package com.alphadot.payroll.service;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.Employee;
import com.alphadot.payroll.model.LeaveTime;
import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.repository.EmployeeRepo;
import com.alphadot.payroll.repository.LeaveTimeRepo;
import com.alphadot.payroll.repository.TimeSheetRepo;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.color.Color;

@Service
public class PayRollService {

	@Autowired
	private EmployeeRepo employeeRepo;

	@Autowired
	private TimeSheetRepo timeSheetRepo;
	
	@Autowired
	private LeaveTimeRepo leaveTimeRepo;
	
	public String createPaySlip(int empId, String month) throws FileNotFoundException, MalformedURLException {
	int yourWorkingDays =0;
	Map<String,Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	map.put("January", 1);
	map.put("February", 2);
	map.put("March", 3);
	map.put("April", 4);
	map.put("April", 5);
	map.put("June", 6);
	map.put("July", 7);
	map.put("August", 8);
	map.put("September", 9);
	map.put("October", 10);
	map.put("November", 11);
	map.put("December", 12);
	LocalDate currentdate = LocalDate.now();
	String year = String.valueOf(currentdate.getYear());
	int leaves = 0;
	try {
		 Employee emp = new Employee();
		  emp = employeeRepo.findByEmpId(empId);
		List<TimeSheetModel> timeSheetModel= timeSheetRepo.search(empId, month.toUpperCase(),year);
		List<LeaveTime> leaveModel= leaveTimeRepo.findByEmpIdAndMonth(empId, month.toUpperCase());
		for(LeaveTime l : leaveModel) {
			leaves++;
		}
		for(TimeSheetModel t : timeSheetModel) {
			yourWorkingDays++;
		}
		 DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String monthDate= String.valueOf(map.get(month));
		   LocalDateTime now = LocalDateTime.now();
		   String firstDayMonth = "01/"+monthDate+"/"+Util.Year;

		String lastDayOfMonth = (LocalDate.parse(firstDayMonth, DateTimeFormatter.ofPattern("dd/M/yyyy"))
		       .with(TemporalAdjusters.lastDayOfMonth())).format(DateTimeFormatter.ofPattern("dd/M/yyyy"));
		  
		String path = "C:\\Users\\hp\\Desktop\\salarys_slips\\" + emp.getFirstName()+emp.getLastName() + "_" + month + ".pdf";

		int grossSalary = 15000;
		int totalWorkingDays = leaves + yourWorkingDays; 
		ImageData datas = ImageDataFactory.create(Util.ImagePath);
		Image alpha = new Image(datas);
		PdfWriter pdfWriter = new PdfWriter(path);
		PdfDocument pdfDocument = new PdfDocument(pdfWriter);
		Document document = new Document(pdfDocument);
		pdfDocument.setDefaultPageSize(PageSize.A4);
		  float col= 250f;
	        float columnWidth[] = {col,col};
	        Table table = new Table(columnWidth);
	        table.setBackgroundColor(new DeviceRgb(63,169,219)).setFontColor(Color.WHITE);
	        table.addCell(new Cell().add("Pay Slip").setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE).setMarginTop(30f).setMarginBottom(30f).setFontSize(30f).setBorder(Border.NO_BORDER));
	       table.addCell(new Cell().add(Util.ADDRESS).setTextAlignment(TextAlignment.RIGHT).setMarginTop(30f).setMarginBottom(30f).setBorder(Border.NO_BORDER).setMarginRight(10f));
	       float colWidth[]={150,150,100,100};
	       Table employeeTable = new Table(colWidth);
	       employeeTable.addCell(new Cell(0,4).add(Util.EmployeeInformation).setBold());
	       employeeTable.addCell(new Cell().add(Util.EmployeeInformation).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add(String.valueOf(emp.getEmpId())).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add(Util.Date).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add(dtf.format(now)).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add(Util.Name).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add(emp.getFirstName()+" "+emp.getLastName()).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add(Util.BankName).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add("State Bank Of India").setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add(Util.JobTitle).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add(emp.getDesignation()).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add(Util.AccountNumber).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add("345029756").setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell().add(Util.MobileNo).setBorder(Border.NO_BORDER));
	        employeeTable.addCell(new Cell(0,4).add("8978568743").setBorder(Border.NO_BORDER));
    Table itemInfo = new Table(columnWidth);
		  itemInfo.addCell(new Cell().add(Util.PayPeriods));
		  itemInfo.addCell(new Cell().add(firstDayMonth+" - "+lastDayOfMonth));
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
	return Util.Successfull;
	}
	catch(Exception e) {
		return "error";
	}
		
	}

	public String saveLeave(int empId,List<String> list) {
		char c=' ';
		Map<String,String> map = new TreeMap<>();
		map.put("1", "January");
		map.put("2", "February");
		map.put("3", "March");
		map.put("4", "April");
		map.put("5", "May");
		map.put("6", "June");
		map.put("7", "July");
		map.put("8", "August");
		map.put("9", "September");
		map.put("10", "October");
		map.put("11", "November");
		map.put("12", "December");
		for(String date :list) {
			 c= date.charAt(6);
			LeaveTime leaveTime = new LeaveTime();
			leaveTime.setDate(date);
			leaveTime.setEmpId(empId);
			leaveTime.setMonth(map.get(String.valueOf(c)));
			leaveTimeRepo.save(leaveTime);
		}	
		return "save";
	}
}