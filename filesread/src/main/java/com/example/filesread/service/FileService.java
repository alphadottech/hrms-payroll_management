package com.example.filesread.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
public class FileService {
	public String readExcelFile(String name, String month) throws IOException {
		int workingDays = 0;
		int leaveDays = 0;
		String leave = "yes";
		String monthName = "";
		String startTime = "";
		String endTime = "";
		String date = "";
		String leaveStatus = "";
		int grossSalary = 15000;
		String payPeriod = "";
		Date endPayPeriod = null;
		Date startPayPeriods = null;
		boolean status = true;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		LocalDateTime now = LocalDateTime.now();
		String dat = String.valueOf(dtf.format(now));

		String projDir = System.getProperty("user.dir");

		// excel read and extract data
		String excelPath = "C:\\Users\\hp\\Downloads\\filesread\\filesread\\datasource\\Vikalp.xlsx";

		XSSFWorkbook workbook = new XSSFWorkbook(excelPath);

		DataFormatter dataFormatter = new DataFormatter();
		XSSFSheet sheet = workbook.getSheet(name);

		for (int i = 1; i <= 366; i++) {
			XSSFRow row = sheet.getRow(i);

			XSSFCell xssMonth = sheet.getRow(i).getCell(1);

			if (month.equalsIgnoreCase(dataFormatter.formatCellValue(xssMonth))) {

				if ((!(dataFormatter.formatCellValue(row.getCell(4)).equals(""))
						&& (!(dataFormatter.formatCellValue(row.getCell(5)).equals(""))))) {

					try {
						if (status) {
							startPayPeriods = new SimpleDateFormat("MM/dd/yyyy")
									.parse(dataFormatter.formatCellValue(row.getCell(2)));
							status = false;
						}

						endPayPeriod = new SimpleDateFormat("MM/dd/yyyy")
								.parse(dataFormatter.formatCellValue(row.getCell(2)));

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					monthName = dataFormatter.formatCellValue(row.getCell(1));
					startTime = dataFormatter.formatCellValue(row.getCell(4));
					endTime = dataFormatter.formatCellValue(row.getCell(5));
					date = dataFormatter.formatCellValue(row.getCell(2));

					workingDays++;
				} else {
					leaveStatus = dataFormatter.formatCellValue(row.getCell(6));
					leaveStatus = leaveStatus.replaceAll("\\s", "");
					if (leaveStatus.equalsIgnoreCase(leave)) {
						leaveDays++;
					}
					leaveStatus = dataFormatter.formatCellValue(row.getCell(6));
					leaveStatus = leaveStatus.replaceAll("\\s", "");
					monthName = dataFormatter.formatCellValue(row.getCell(1));
					date = dataFormatter.formatCellValue(row.getCell(2));

				}

			}

		}
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
		String enDate = formatter.format(endPayPeriod);

		SimpleDateFormat formatters = new SimpleDateFormat("dd/MM/yy");
		String strDate = formatter.format(startPayPeriods);

		payPeriod = strDate + "-" + enDate;

		// pdf creation

		String path = "C:\\Users\\hp\\Desktop\\salarys_slips\\" + name + "_" + month + ".pdf";
		String imgsrc = "C:\\Users\\hp\\Downloads\\filesread\\filesread\\images\\alpha.png";
		ImageData datas = ImageDataFactory.create(imgsrc);
		Image alpha = new Image(datas);
		PdfWriter pdfWriter = new PdfWriter(path);
		PdfDocument pdfDocument = new PdfDocument(pdfWriter);
		Document document = new Document(pdfDocument);
		pdfDocument.setDefaultPageSize(PageSize.A4);

		float col = 560f;
		float columnWidth[] = { col };
		String para = "Alpha Dot Technologies\n Address: MPSEDC STP Building, Electronic Complex, Sukhlia,\nIndore, Madhya Pradesh 452003\rContact Number: 0731-4275767";
		Paragraph paras = new Paragraph(para);
		Table table = new Table(columnWidth);

		table.setBackgroundColor(new DeviceRgb(63, 169, 219));

		table.addCell("Salary Slip").setTextAlignment(TextAlignment.CENTER);

		float colWidth[] = { 280, 280 };
		float colm[] = { 560 };
		Table tab = new Table(colm).setBorder(Border.NO_BORDER);
		tab.addCell("Employee Information"
				+ "                                                                                  " + "Date: " + dat)
				.setBold().setBorder(Border.NO_BORDER);

		Table customerInfoTable = new Table(colWidth);
		customerInfoTable.addCell("Name");
		customerInfoTable.addCell(name);
		customerInfoTable.addCell("Account Number");
		customerInfoTable.addCell("32770338019");
		customerInfoTable.addCell("Bank Name");
		customerInfoTable.addCell("State Bank Of India");
		customerInfoTable.addCell("Pay Period");
		customerInfoTable.addCell(payPeriod);
		customerInfoTable.addCell("Number Of working days");
		customerInfoTable.addCell(String.valueOf(workingDays));
		customerInfoTable.addCell("Number of leaves taken");
		customerInfoTable.addCell(String.valueOf(leaveDays));
		customerInfoTable.addCell("Amount deducted for leaves");
		customerInfoTable.addCell(String.valueOf((grossSalary / workingDays) * leaveDays));
		customerInfoTable.addCell("Amount Payable per day");
		customerInfoTable.addCell(String.valueOf(grossSalary / (workingDays+leaveDays)));
		customerInfoTable.addCell("Gross salary");
		customerInfoTable.addCell(String.valueOf(grossSalary));
		customerInfoTable.addCell("Net amount payable");
		customerInfoTable.addCell(String.valueOf((grossSalary - (grossSalary / workingDays) * leaveDays)));
		document.add(alpha);
		document.add(paras);
		document.add(table);
		document.add(tab);
        document.add(customerInfoTable);
		document.close();
		return "ok";

	}

}
