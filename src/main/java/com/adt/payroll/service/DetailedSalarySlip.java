package com.adt.payroll.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import com.adt.payroll.model.PaySlip;
import com.adt.payroll.model.SalaryDetails;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class DetailedSalarySlip {
	
	private static final Logger log = LogManager.getLogger(DetailedSalarySlip.class);

    public ByteArrayOutputStream generateDetailedSalarySlipPDF(String empId, String name, int totalWorkingDays, int present,
                                                               int leave, int halfDay, String salary, String paidLeave, String date, String bankName,
                                                               String accountNumber, String designation, String joiningDate, int adhoc1, String payPeriod, float esicAmount, float pfAmount, double netAmount, double grossSalary, double basic, double hra, double amountPerDay, double unpaidLeave, int adjustment, int medicalInsurance,int tds) throws DocumentException, IOException {
        LocalDate currentdate = LocalDate.now();
        LocalDate earlier = currentdate.minusMonths(1);
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();
        double paidleave = amountPerDay * Integer.parseInt(paidLeave);
        double halfDayAmount = ((double) halfDay / 2)* amountPerDay;
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingBefore(10f);
        headerTable.setSpacingAfter(10f);
        // Logo Cell
        PdfPCell logoCell = new PdfPCell();
       InputStream stream = new ClassPathResource("image/alphadot_tech_logo.jpg").getInputStream();
        Image logo = Image.getInstance(IOUtils.toByteArray(stream));
        logo.scaleToFit(130, 110); // Adjust the size of the logo as needed
        logoCell.addElement(logo); // Add the logo to the cell
        logoCell.setBorder(Rectangle.NO_BORDER); // Remove cell border
        headerTable.addCell(logoCell);
        // Add Header
        PdfPCell companyNameCell = new PdfPCell();
        Paragraph companyAdd = new Paragraph();
        Paragraph companyName = new Paragraph();
        companyName.add(new Phrase("Alpha Dot Technologies Pvt ltd.", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)));
        companyAdd.add(new Phrase(Util.ADDRESS2, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK)));
        companyAdd.add(Chunk.NEWLINE);

        // companyName.add(new Paragraph("Salary SLip",FontFactory.getFont(FontFactory.HELVETICA_BOLD,16,BaseColor.RED)));
        companyNameCell.addElement(companyName);
        companyNameCell.addElement(companyAdd);
        // Add the company name to the cell
        companyNameCell.setBorder(Rectangle.NO_BORDER);

        headerTable.addCell(companyNameCell);
        Paragraph paragraph = new Paragraph();
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
        Chunk chunk = new Chunk("Pay Slip For The Month Of " + String.valueOf(earlier.getMonth()).toLowerCase(), font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(chunk);
        document.add(headerTable);
        document.add(paragraph);

        PdfContentByte canvas = writer.getDirectContent();
        canvas.moveTo(50, document.getPageSize().getHeight() - 132); // Adjust position as needed
        canvas.lineTo(document.getPageSize().getWidth() - 50, document.getPageSize().getHeight() - 132); // Adjust position as needed
        canvas.setColorStroke(BaseColor.BLACK); // Line color
        canvas.setLineWidth(1f); // Line width
        canvas.stroke();

        float borderWidth = 35f; // Adjust the border width as needed
        float documentWidth = PageSize.A4.getWidth();
        float documentHeight = PageSize.A4.getHeight();

// Calculate coordinates for the border rectangle
        float borderLeftX = borderWidth;
        float borderRightX = documentWidth - borderWidth;
        float borderBottomY = borderWidth + 220;
        float borderTopY = documentHeight - borderWidth;

// Draw a smaller rectangle around the content
        PdfContentByte box = writer.getDirectContent();
        box.rectangle(borderLeftX, borderBottomY, borderRightX - borderLeftX, borderTopY - borderBottomY);
        box.setColorStroke(BaseColor.BLACK); // Set border color
        box.setLineWidth(1f); // Set border line width
        box.stroke();
        // Add Employee Details
        PdfPTable empDetailsTable = new PdfPTable(4); // Updated to accommodate bank details horizontally
        empDetailsTable.setWidthPercentage(100);
        empDetailsTable.setSpacingBefore(10f);
        empDetailsTable.setSpacingAfter(10f);

        addTableCell2(empDetailsTable, "Employee Name", name, 2);
        addTableCell2(empDetailsTable, "Employee ID", empId, 2);
        addTableCell2(empDetailsTable, "Designation", designation, 2);
        addTableCell2(empDetailsTable, "Joining Date", joiningDate, 2);
        addTableCell2(empDetailsTable, "Pay Period", payPeriod, 2);
        addTableCell2(empDetailsTable, "Bank Name", bankName, 2);
        addTableCell2(empDetailsTable, "Account Number", accountNumber, 2);

        document.add(empDetailsTable);

        // Add Details Section (including reimbursement, working days, and present days)
        PdfPTable detailsTable = new PdfPTable(6);
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingBefore(10f);
        detailsTable.setSpacingAfter(10f);
        double absent = totalWorkingDays - present;
        addTableCell(detailsTable, "Absent", String.valueOf(absent), 1);
        addTableCell(detailsTable, "Number of Working Days", String.valueOf(totalWorkingDays), 1);
        addTableCell(detailsTable, "Present Days", String.valueOf(present), 1);
        addTableCell(detailsTable, "Paid Leave", paidLeave, 1);
        addTableCell(detailsTable, "Half Day", String.valueOf(halfDay), 1);
        addTableCell(detailsTable, "Unpaid Leave", String.valueOf(leave), 1);
        // addEmptyCell(detailsTable); // Empty cell to align with the employee details

        document.add(detailsTable);

        // Create Main Table to Hold Earnings and Deductions Side by Side
        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setSpacingBefore(10f);
        mainTable.setSpacingAfter(10f);

        PdfPTable earningsTable = createParallelTable("Earnings", "Basic Salary", String.valueOf(basic), "Hra", String.valueOf(hra), "Adhoc", String.valueOf(adhoc1));
        PdfPCell earningsCell = new PdfPCell(earningsTable);
        double grossEarning = basic + hra + adhoc1;
        mainTable.addCell(earningsCell);
        // Add Deductions Table

        // PdfPTable deductionsTable =    createParallelTable("Deductions", "Esic Deduction", String.valueOf(esicAmount), "PF Deduction", String.valueOf(pfAmount),"Adjustment",String.valueOf(adjustment), "Unpaid Leave", String.valueOf(Math.round(unpaidLeave)));
        PdfPTable deductionsTable = medicalInsurance == 0 ? createParallelTable("Deductions", "Esic Deduction", String.valueOf(esicAmount), "PF Deduction", String.valueOf(pfAmount), "Adjustment", String.valueOf(adjustment), "Absent Deduction", String.valueOf(Math.round(unpaidLeave-halfDayAmount)),"TDS", String.valueOf(tds)) : createParallelTable("Deductions", "Esic Deduction", String.valueOf(esicAmount), "PF Deduction", String.valueOf(pfAmount), "Adjustment", String.valueOf(adjustment), "Absent Amount", String.valueOf(Math.round(unpaidLeave)), "Medical Insurance", String.valueOf(medicalInsurance), "TDS", String.valueOf(tds));
        PdfPCell deductionsCell = new PdfPCell(deductionsTable);
        double grossDeduction = esicAmount + pfAmount + (unpaidLeave-halfDayAmount) + adjustment + medicalInsurance+tds;

        mainTable.addCell(deductionsCell);
        document.add(mainTable);

        // Add Gross Earnings, Gross Deductions, and Net Pay
        double netPay = grossEarning - grossDeduction;
        PdfPTable grossTable = new PdfPTable(4);
        grossTable.setWidthPercentage(100);
        grossTable.setSpacingBefore(10f);
        grossTable.setSpacingAfter(10f);
        addTableCell(grossTable, "Gross Earnings", String.valueOf(Math.round(grossEarning)), 1);
        addTableCell(grossTable, "Gross Deductions", String.valueOf(Math.round(grossDeduction)), 1);
        addTableCell(grossTable, "Net Pay", String.valueOf(Math.round(netPay)), 3);
        document.add(grossTable);
        document.add(new Paragraph("\n(Note - This is a computer generated statement and does not require a signature.)"));
        double salaryCastInDouble = Double.parseDouble(salary);
        double ctc = Math.round(salaryCastInDouble * 12);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
//        document.add(new Paragraph("\n\nCalculation – \n" +
//                "CTC = " + salaryCastInDouble + "*12 = " + ctc + "\n" +
//                "Gross = " + salaryCastInDouble + " – (" + salaryCastInDouble + "/2)*0.13 – (" + salaryCastInDouble + ")* (0.04) + (" + salaryCastInDouble + ")* (0.01617) = " + grossSalary + "(if esic yes)\n" +
//                "Basic = " + grossSalary + " / 2 = " + basic + "\n" +
//                "Hra = " + grossSalary + " /2 = " + hra + "\n" +
//                "Esic = (" + grossSalary + ") *0.0075 = " + esicAmount + "\n" +
//                "EPf = (" + grossSalary + ") *0.120 = " + pfAmount + "\n" +
//                "Amount per day = " + grossSalary + " / total working day for this month =" + Math.round(amountPerDay) + "\n", smallFont));

        document.close();
        return outputStream;
    }

    private static PdfPTable createParallelTable(String title, String... items) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        PdfPCell headerCell = new PdfPCell(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        headerCell.setBackgroundColor(BaseColor.WHITE);
        headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerCell.setPaddingLeft(100);
        headerCell.setColspan(2);
        table.addCell(headerCell);

        for (int i = 0; i < items.length; i += 2) {
            addTableCell2(table, items[i], items[i + 1], 1);
        }

        return table;
    }

    private static void addTableCell(PdfPTable table, String title, String value, int colspan) {
        PdfPCell cellTitle = new PdfPCell(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cellTitle.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellTitle.setColspan(colspan);
        table.addCell(cellTitle);

        PdfPCell cellValue = new PdfPCell(new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValue.setColspan(colspan);
        table.addCell(cellValue);
    }

    private static void addTableCell2(PdfPTable table, String title, String value, int colspan) {
        PdfPCell cellTitle = new PdfPCell(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cellTitle.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellTitle.setColspan(colspan);
        table.addCell(cellTitle).setBorder(Rectangle.NO_BORDER);

        PdfPCell cellValue = new PdfPCell(new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValue.setColspan(colspan);
        cellValue.setPaddingRight(10);
        table.addCell(cellValue).setBorder(Rectangle.NO_BORDER);
    }
    
 // mpdified code
 	public ByteArrayOutputStream generateDetailedSalarySlipPDF(SalaryDetails salaryDetails, PaySlip paySlip, String joiningDate, String currentMonth, double adjustment)
 			throws DocumentException, IOException {
 		
 		log.info("generateDetailedSalarySlipPDF:---");
 		
         Document document = new Document(PageSize.A4, 50, 50, 50, 50);
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         PdfWriter writer = PdfWriter.getInstance(document, outputStream);
         document.open();

         PdfPTable headerTable = new PdfPTable(2);
         headerTable.setWidthPercentage(100);
         headerTable.setSpacingBefore(10f);
         headerTable.setSpacingAfter(10f);
         // Logo Cell
         PdfPCell logoCell = new PdfPCell();
         InputStream stream = new ClassPathResource("image/alphadot_tech_logo.jpg").getInputStream();
         Image logo = Image.getInstance(IOUtils.toByteArray(stream));
         logo.scaleToFit(130, 110); // Adjust the size of the logo as needed
         logoCell.addElement(logo); // Add the logo to the cell
         logoCell.setBorder(Rectangle.NO_BORDER); // Remove cell border
         headerTable.addCell(logoCell);
         // Add Header
         PdfPCell companyNameCell = new PdfPCell();
         Paragraph companyAdd = new Paragraph();
         Paragraph companyName = new Paragraph();
         companyName.add(new Phrase("Alpha Dot Technologies Pvt ltd.", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)));
         companyAdd.add(new Phrase(Util.ADDRESS2, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK)));
         companyAdd.add(Chunk.NEWLINE);

         // companyName.add(new Paragraph("Salary SLip",FontFactory.getFont(FontFactory.HELVETICA_BOLD,16,BaseColor.RED)));
         companyNameCell.addElement(companyName);
         companyNameCell.addElement(companyAdd);
         // Add the company name to the cell
         companyNameCell.setBorder(Rectangle.NO_BORDER);

         headerTable.addCell(companyNameCell);
         Paragraph paragraph = new Paragraph();
         Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
         Chunk chunk = new Chunk("Pay Slip For The Month Of " + currentMonth, font);
         paragraph.setAlignment(Element.ALIGN_CENTER);
         paragraph.add(chunk);
         document.add(headerTable);
         document.add(paragraph);

         PdfContentByte canvas = writer.getDirectContent();
         canvas.moveTo(50, document.getPageSize().getHeight() - 132); // Adjust position as needed
         canvas.lineTo(document.getPageSize().getWidth() - 50, document.getPageSize().getHeight() - 132); // Adjust position as needed
         canvas.setColorStroke(BaseColor.BLACK); // Line color
         canvas.setLineWidth(1f); // Line width
         canvas.stroke();

         float borderWidth = 35f; // Adjust the border width as needed
         float documentWidth = PageSize.A4.getWidth();
         float documentHeight = PageSize.A4.getHeight();

 // Calculate coordinates for the border rectangle
         float borderLeftX = borderWidth;
         float borderRightX = documentWidth - borderWidth;
         float borderBottomY = borderWidth + 220;
         float borderTopY = documentHeight - borderWidth;

 // Draw a smaller rectangle around the content
         PdfContentByte box = writer.getDirectContent();
         box.rectangle(borderLeftX, borderBottomY, borderRightX - borderLeftX, borderTopY - borderBottomY);
         box.setColorStroke(BaseColor.BLACK); // Set border color
         box.setLineWidth(1f); // Set border line width
         box.stroke();
         // Add Employee Details
         PdfPTable empDetailsTable = new PdfPTable(4); // Updated to accommodate bank details horizontally
         empDetailsTable.setWidthPercentage(100);
         empDetailsTable.setSpacingBefore(10f);
         empDetailsTable.setSpacingAfter(10f);
         
         addTableCell2(empDetailsTable, "Employee Name", paySlip.getName(), 2);
         addTableCell2(empDetailsTable, "Employee ID", String.valueOf(salaryDetails.getEmpId()), 2);
         addTableCell2(empDetailsTable, "Designation", paySlip.getJobTitle(), 2);
         addTableCell2(empDetailsTable, "Joining Date", joiningDate, 2);
         addTableCell2(empDetailsTable, "Pay Period", paySlip.getPayPeriods(), 2);
         addTableCell2(empDetailsTable, "Bank Name", paySlip.getBankName(), 2);
         addTableCell2(empDetailsTable, "Account Number", paySlip.getAccountNumber(), 2);

         document.add(empDetailsTable);

         // Add Details Section (including reimbursement, working days, and present days)
         PdfPTable detailsTable = new PdfPTable(6);
         detailsTable.setWidthPercentage(100);
         detailsTable.setSpacingBefore(10f);
         detailsTable.setSpacingAfter(10f);
       //  double absent = totalWorkingDays - present;
         addTableCell(detailsTable, "Absent", String.valueOf(paySlip.getNumberOfLeavesTaken()), 1);
         addTableCell(detailsTable, "Number of Working Days", String.valueOf(paySlip.getTotalWorkingDays()), 1);
         addTableCell(detailsTable, "Present Days", String.valueOf(paySlip.getYouWorkingDays()), 1);
         addTableCell(detailsTable, "Paid Leave", paySlip.getPaidLeave().toString(), 1);
         addTableCell(detailsTable, "Half Day", String.valueOf(paySlip.getHalfday()), 1);
//         addTableCell(detailsTable, "Unpaid Leave", String.valueOf(leave), 1);
         addTableCell(detailsTable, "Unpaid Leave", String.valueOf(paySlip.getUnpaidLeave()), 1);
         // addEmptyCell(detailsTable); // Empty cell to align with the employee details

         document.add(detailsTable);

         // Create Main Table to Hold Earnings and Deductions Side by Side
         PdfPTable mainTable = new PdfPTable(2);
         mainTable.setWidthPercentage(100);
         mainTable.setSpacingBefore(10f);
         mainTable.setSpacingAfter(10f);

         PdfPTable earningsTable = createParallelTable("Earnings", "Basic Salary", String.valueOf(salaryDetails.getBasic()), "Hra", String.valueOf(salaryDetails.getHouseRentAllowance()), "Adhoc", String.valueOf(0 ));//adhoc1));
         PdfPCell earningsCell = new PdfPCell(earningsTable);
//         double grossEarning = basic + hRA + adhoc1;
         double grossEarning = salaryDetails.getGrossSalary();
         mainTable.addCell(earningsCell);
         // Add Deductions Table

         // PdfPTable deductionsTable =    createParallelTable("Deductions", "Esic Deduction", String.valueOf(esicAmount), "PF Deduction", String.valueOf(pfAmount),"Adjustment",String.valueOf(adjustment), "Unpaid Leave", String.valueOf(Math.round(unpaidLeave)));
//         PdfPTable deductionsTable = medicalInsurance == 0 ? createParallelTable("Deductions", "Esic Deduction", String.valueOf(esicAmount), "PF Deduction", String.valueOf(pfAmount), "Adjustment", String.valueOf(adjustment), "Absent Deduction", String.valueOf(Math.round(absentDeduction)),"TDS", String.valueOf(tds)) : createParallelTable("Deductions", "Esic Deduction", String.valueOf(esicAmount), "PF Deduction", String.valueOf(pfAmount), "Adjustment", String.valueOf(adjustment), "Absent Amount", String.valueOf(Math.round(unpaidLeave)), "Medical Insurance", String.valueOf(medicalInsurance), "TDS", String.valueOf(tds));
//         PdfPCell deductionsCell = new PdfPCell(deductionsTable);
//         double grossDeduction = esicAmount + pfAmount + (unpaidLeaveAmount-halfDayAmount) + adjustment + medicalInsurance + tds;
         PdfPTable deductionsTable =  createParallelTable("Deductions", "Esic Deduction", String.valueOf(salaryDetails.getEmployeeESICAmount()), "PF Deduction", String.valueOf(salaryDetails.getEmployeePFAmount()), "Adjustment", String.valueOf(adjustment), "Absent Deduction", String.valueOf(Math.round(paySlip.getLeaveDeductionAmount())), "Medical Insurance", String.valueOf(salaryDetails.getMedicalInsurance()));
         PdfPCell deductionsCell = new PdfPCell(deductionsTable);
         double grossDeduction = salaryDetails.getEmployeeESICAmount() + salaryDetails.getEmployeePFAmount() + paySlip.getLeaveDeductionAmount() + salaryDetails.getMedicalInsurance();
         mainTable.addCell(deductionsCell);
         document.add(mainTable);

         // Add Gross Earnings, Gross Deductions, and Net Pay
//         double netPay = grossEarning - grossDeduction;
         double netPay = paySlip.getNetSalaryAmount() ;
         PdfPTable grossTable = new PdfPTable(4);
         grossTable.setWidthPercentage(100);
         grossTable.setSpacingBefore(10f);
         grossTable.setSpacingAfter(10f);
         addTableCell(grossTable, "Gross Earnings", String.valueOf(Math.round(grossEarning)), 1);
         addTableCell(grossTable, "Gross Deductions", String.valueOf(Math.round(grossDeduction)), 1);
         addTableCell(grossTable, "Net Pay", String.valueOf(Math.round(netPay)), 3);
         document.add(grossTable);
         document.add(new Paragraph("\n(Note - This is a computer generated statement and does not require a signature.)"));
       //  double salaryCastInDouble = paySlip.getSalary();
     //    double ctc = Math.round(salaryCastInDouble * 12);
         Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
//         document.add(new Paragraph("\n\nCalculation – \n" +
//                 "CTC = " + salaryCastInDouble + "*12 = " + ctc + "\n" +
//                 "Gross = " + salaryCastInDouble + " – (" + salaryCastInDouble + "/2)*0.13 – (" + salaryCastInDouble + ")* (0.04) + (" + salaryCastInDouble + ")* (0.01617) = " + grossSalary + "(if esic yes)\n" +
//                 "Basic = " + grossSalary + " / 2 = " + basic + "\n" +
//                 "Hra = " + grossSalary + " /2 = " + hra + "\n" +
//                 "Esic = (" + grossSalary + ") *0.0075 = " + esicAmount + "\n" +
//                 "EPf = (" + grossSalary + ") *0.120 = " + pfAmount + "\n" +
//                 "Amount per day = " + grossSalary + " / total working day for this month =" + Math.round(amountPerDay) + "\n", smallFont));

         document.close();
     	log.info("generateDetailedSalarySlipPDF-outputStream:---"+outputStream);
         return outputStream;
     }
}
