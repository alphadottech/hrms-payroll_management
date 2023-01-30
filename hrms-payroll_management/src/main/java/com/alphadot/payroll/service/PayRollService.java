package com.alphadot.payroll.service;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
@Service
public class PayRollService {
	public String readExcelFile(String name, String month, int addOn, String midPeriod, int add)
			throws IOException, ParseException {
		int beforeDays = 0;
		int afterDays = 0;
		int workingDays = 0;
		int leaveDays = 0;
		int beforeLeaveDays = 0;
		int afterLeaveDays = 0;
		String leave = "yes";
		String monthName = "";
		String startTime = "";
		String endTime = "";
		String date = "";
		String leaveStatus = "";
		int grossSalary = PayInfo.GSalary;
		String payPeriod = "";
		String payPeriods = "";
		Date endPayPeriod = null;
		Date endPayPeriods = null;
		Date startPayPeriods = null;
		String enDate = "";
		String strDate = "";

		SimpleDateFormat DateFor = new SimpleDateFormat("MM/dd/yyyy");

		SimpleDateFormat DateFors = new SimpleDateFormat("MM/dd/yyyy");

		boolean status = true;

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDateTime now = LocalDateTime.now();
		String dat = String.valueOf(dtf.format(now));

		String projDir = System.getProperty("user.dir");
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

		XSSFWorkbook workbook = new XSSFWorkbook(Util.ExcelPath);

		DataFormatter dataFormatter = new DataFormatter();
		dataFormatter.addFormat("m/d/yy", new java.text.SimpleDateFormat("M/d/yyyy"));
		XSSFSheet sheet = workbook.getSheet(name);

		try {
			Date midDate = new SimpleDateFormat("MM/dd/yyyy").parse(midPeriod);
			LocalDate parsedDate = midDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate newDate = parsedDate.minusDays(1);
			Date x = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			String newDates = formatter.format(x);
			Date dates = DateFor.parse(midPeriod);
			formatter.format(dates);
			// excel read and extract data
			for (int i = 1; i <= 366; i++) {
				XSSFRow row = sheet.getRow(i);
				try {
					XSSFCell xssMonth = sheet.getRow(i).getCell(1);

					if (month.equalsIgnoreCase(dataFormatter.formatCellValue(xssMonth))) {
						if (status) {
							startPayPeriods = new SimpleDateFormat("MM/dd/yyyy")
									.parse(dataFormatter.formatCellValue(row.getCell(2)));
							status = false;
						}

						date = dataFormatter.formatCellValue(row.getCell(2));

						if (DateFor.parse(midPeriod).compareTo(DateFors.parse(date)) > 0) {

							if ((!(dataFormatter.formatCellValue(row.getCell(4)).equals(""))
									&& (!(dataFormatter.formatCellValue(row.getCell(5)).equals(""))))) {

								endPayPeriod = new SimpleDateFormat("MM/dd/yyyy")
										.parse(dataFormatter.formatCellValue(row.getCell(2)));

								startTime = dataFormatter.formatCellValue(row.getCell(4));
								endTime = dataFormatter.formatCellValue(row.getCell(5));

								beforeDays++;
							} else {
								leaveStatus = dataFormatter.formatCellValue(row.getCell(6));
								leaveStatus = leaveStatus.replaceAll("\\s", "");
								if (leaveStatus.equalsIgnoreCase(leave)) {
									beforeLeaveDays++;
								}
								leaveStatus = dataFormatter.formatCellValue(row.getCell(6));
								leaveStatus = leaveStatus.replaceAll("\\s", "");

							}
						} else {
							if ((!(dataFormatter.formatCellValue(row.getCell(4)).equals(""))
									&& (!(dataFormatter.formatCellValue(row.getCell(5)).equals(""))))) {

								endPayPeriod = new SimpleDateFormat("MM/dd/yyyy")
										.parse(dataFormatter.formatCellValue(row.getCell(2)));

								startTime = dataFormatter.formatCellValue(row.getCell(4));
								endTime = dataFormatter.formatCellValue(row.getCell(5));

								afterDays++;
							} else {
								leaveStatus = dataFormatter.formatCellValue(row.getCell(6));
								leaveStatus = leaveStatus.replaceAll("\\s", "");
								if (leaveStatus.equalsIgnoreCase(leave)) {
									afterLeaveDays++;
								}
								leaveStatus = dataFormatter.formatCellValue(row.getCell(6));
								leaveStatus = leaveStatus.replaceAll("\\s", "");

							}
						}

					}
				} catch (Exception e) {
					break;
				}
			}
			workingDays = beforeDays + afterDays;
			leaveDays = beforeLeaveDays + afterLeaveDays;

			enDate = formatter.format(endPayPeriod);

			strDate = formatter.format(startPayPeriods);

			payPeriod = midPeriod + "-" + enDate;

			payPeriods = strDate + "-" + newDates;
			return pdfGenerates(payPeriod, payPeriods, grossSalary, add, beforeDays, afterDays, workingDays,
					afterLeaveDays, beforeLeaveDays, leaveDays, name, addOn, month, dat);

		} catch (Exception e) {
			// excel read and extract data
			for (int i = 1; i <= 366; i++) {
				XSSFRow row = sheet.getRow(i);
				try {
					XSSFCell xssMonth = sheet.getRow(i).getCell(1);

					if (month.equalsIgnoreCase(dataFormatter.formatCellValue(xssMonth))) {
						if (status) {
							startPayPeriods = new SimpleDateFormat("MM/dd/yyyy")
									.parse(dataFormatter.formatCellValue(row.getCell(2)));
							status = false;
						}
						date = dataFormatter.formatCellValue(row.getCell(2));

						if ((!(dataFormatter.formatCellValue(row.getCell(4)).equals(""))
								&& (!(dataFormatter.formatCellValue(row.getCell(5)).equals(""))))) {

							endPayPeriod = new SimpleDateFormat("MM/dd/yyyy")
									.parse(dataFormatter.formatCellValue(row.getCell(2)));

							startTime = dataFormatter.formatCellValue(row.getCell(4));
							endTime = dataFormatter.formatCellValue(row.getCell(5));

							workingDays++;
						} else {
							leaveStatus = dataFormatter.formatCellValue(row.getCell(6));
							leaveStatus = leaveStatus.replaceAll("\\s", "");
							if (leaveStatus.equalsIgnoreCase(leave)) {
								leaveDays++;
							}
							leaveStatus = dataFormatter.formatCellValue(row.getCell(6));
							leaveStatus = leaveStatus.replaceAll("\\s", "");

						}

					}
				} catch (Exception k) {
					break;
				}

			}

			enDate = formatter.format(endPayPeriod);

			strDate = formatter.format(startPayPeriods);

			payPeriod = strDate + "-" + enDate;

			return pdfGenerate(payPeriod, grossSalary, add, leaveDays, name, addOn, workingDays, month, dat);

		}

	}

	private String pdfGenerates(String payPeriod, String payPeriods, int grossSalary, int add, int beforeDays,
			int afterDays, int workingDays, int afterLeaveDays, int beforeLeaveDays, int leaveDays, String name,
			int addOn, String month, String dat) throws MalformedURLException, FileNotFoundException {
		String path = "C:\\Users\\HP\\Downloads\\salarys_slips\\" + name + "_" + month + ".pdf";

		ImageData datas = ImageDataFactory.create(Util.ImagePath);
		Image alpha = new Image(datas);
		PdfWriter pdfWriter = new PdfWriter(path);
		PdfDocument pdfDocument = new PdfDocument(pdfWriter);
		Document document = new Document(pdfDocument);
		pdfDocument.setDefaultPageSize(PageSize.A4);

		int beforePerDay = (grossSalary / (workingDays + leaveDays));
		int afterPerDay = ((grossSalary + add) / (workingDays + leaveDays));
		int before = beforePerDay * beforeDays - (beforePerDay * beforeLeaveDays);
		int after = afterPerDay * afterDays - (afterPerDay * afterLeaveDays);
		float col = 560f;
		float columnWidth[] = { col };

		Paragraph paras = new Paragraph(Util.ADDRESS);
		Table table = new Table(columnWidth);

		table.setBackgroundColor(new DeviceRgb(63, 169, 219));

		table.addCell(Util.PaySlip).setTextAlignment(TextAlignment.CENTER);

		float colm[] = { 560 };
		Table tab = new Table(colm).setBorder(Border.NO_BORDER);
	
		tab.addCell(Util.EmployeeInformation
				+ "                                                                                        " + "Date: " + dat +"\n"+
				
		Util.EmployeeNumber  +"     :     "+ PayInfo.EmpNumber+"                                            "+Util.BankName+" : "+PayInfo.BName+"\n"+
		Util.Name+"                         :     "+name+"                    "+Util.AccountNumber+" : "+PayInfo.ANumber+"\n"+
		Util.Gender +"                       :     "+ PayInfo.Gender+"\n"+
		Util.JobTitle +"                     :     "+ PayInfo.JTitle);
	
		

		float colWidth[] = { 280, 280 };
		float colWidths[] = { 280, 140, 140 };

		Table customerInfoTable = new Table(colWidth);
		Table pay = new Table(colWidths);
	
		
		customerInfoTable.addCell(Util.NumberOfLeavesTaken);
		customerInfoTable.addCell(String.valueOf(leaveDays));
		customerInfoTable.addCell(Util.GrossSalary);
		customerInfoTable.addCell(String.valueOf(grossSalary));
		customerInfoTable.addCell(Util.NewGrossSalary);
		customerInfoTable.addCell(String.valueOf(grossSalary + add));
		customerInfoTable.addCell(Util.BeforePromotion);
		customerInfoTable.addCell(String.valueOf(before));
		customerInfoTable.addCell(Util.AfterPromotion);
		customerInfoTable.addCell(String.valueOf(after));

			pay.addCell(Util.PayPeriods);
			pay.addCell(payPeriods);
			pay.addCell(payPeriod);
			customerInfoTable.addCell(Util.BonusAmount);
			customerInfoTable.addCell(String.valueOf(addOn));
			customerInfoTable.addCell(Util.NetAmountPayable);
			customerInfoTable.addCell(String.valueOf(before + after + addOn));

			document.add(alpha);
			document.add(paras);
			document.add(table);
			document.add(tab);
			document.add(pay);
			document.add(customerInfoTable);
			
			document.close();
			return Util.Successfull;

	}

//Pdf Creation		
	public String pdfGenerate(String payPeriod, int grossSalary, int add, int leaveDays, String name, int addOn,
			int workingDays, String month, String dat) throws FileNotFoundException, MalformedURLException {
		String path = "C:\\Users\\HP\\Downloads\\salarys_slips\\" + name + "_" + month + ".pdf";

		ImageData datas = ImageDataFactory.create(Util.ImagePath);
		Image alpha = new Image(datas);
		PdfWriter pdfWriter = new PdfWriter(path);
		PdfDocument pdfDocument = new PdfDocument(pdfWriter);
		Document document = new Document(pdfDocument);
		pdfDocument.setDefaultPageSize(PageSize.A4);

		float col = 560f;
		float columnWidth[] = { col };

		Paragraph paras = new Paragraph(Util.ADDRESS);
		Table table = new Table(columnWidth);

		table.setBackgroundColor(new DeviceRgb(63, 169, 219));

		table.addCell(Util.PaySlip).setTextAlignment(TextAlignment.CENTER);

		float colm[] = { 560 };
		Table tab = new Table(colm).setBorder(Border.NO_BORDER);
		tab.addCell(Util.EmployeeInformation
				+ "                                                                                        " + "Date: " + dat +"\n"+
				
		Util.EmployeeNumber  +"     :     "+ PayInfo.EmpNumber+"                                            "+Util.BankName+" : "+PayInfo.BName+"\n"+
		Util.Name+"                         :     "+name+"                    "+Util.AccountNumber+" : "+PayInfo.ANumber+"\n"+
		Util.Gender +"                       :     "+ PayInfo.Gender+"\n"+
		Util.JobTitle +"                     :     "+ PayInfo.JTitle);

		float colWidth[] = { 280, 280 };

		Table customerInfoTable = new Table(colWidth);
	
		customerInfoTable.addCell(Util.PayPeriods);
		customerInfoTable.addCell(payPeriod);
		customerInfoTable.addCell(Util.YourWorkingDays);
		customerInfoTable.addCell(String.valueOf(workingDays));
		customerInfoTable.addCell(Util.NumberOfLeavesTaken);
		customerInfoTable.addCell(String.valueOf(leaveDays));
		customerInfoTable.addCell(Util.TotalWorkingDays);
		customerInfoTable.addCell(String.valueOf(workingDays + leaveDays));
		customerInfoTable.addCell(Util.AmountDeductedForLeaves);
		customerInfoTable.addCell(String.valueOf((grossSalary / workingDays) * leaveDays));
		customerInfoTable.addCell(Util.BonusAmount);
		customerInfoTable.addCell(String.valueOf(addOn));
		customerInfoTable.addCell(Util.AmountPayablePerDay);
		customerInfoTable.addCell(String.valueOf(grossSalary / (workingDays + leaveDays)));
		customerInfoTable.addCell(Util.GrossSalary);
		customerInfoTable.addCell(String.valueOf(grossSalary));
		customerInfoTable.addCell(Util.NetAmountPayable);
		customerInfoTable.addCell(String.valueOf((grossSalary - (grossSalary / workingDays) * leaveDays) + addOn));
		document.add(alpha);
		document.add(paras);
		document.add(table);
		document.add(tab);
		document.add(customerInfoTable);
		document.close();

		return Util.Successfull;

	}

}