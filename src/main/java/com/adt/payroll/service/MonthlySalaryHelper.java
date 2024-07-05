package com.adt.payroll.service;

import com.adt.payroll.model.EmpPayrollDetails;
import com.adt.payroll.model.MonthlySalaryDetails;
import com.adt.payroll.repository.EmpPayrollDetailsRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MonthlySalaryHelper {
    public static String[] Headers = {"empId", "employeeName", "employeeESICAmount", "employerESICAmount", "employeePFAmount", "employerPFAmount", "medicalInsurance", "grossSalary","month","adhoc","creditedDate","accountNumber","bankName"};
    public static String Sheet_name = "MonthlySalaryDetails_data";
    @Autowired
    private EmpPayrollDetailsRepo empPayrollDetailsRepo;
    public static ByteArrayInputStream dataToExcel(List<MonthlySalaryDetails> list, Optional<EmpPayrollDetails> list1) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Sheet sheet = workbook.createSheet(Sheet_name);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < Headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(Headers[i]);
                cell.setCellStyle(headerCellStyle);
            }
            int rowIndex = 1;
            for (MonthlySalaryDetails salaryDetails : list) {
                Row rowData = sheet.createRow(rowIndex++);
                rowData.createCell(0).setCellValue(salaryDetails.getEmpId());
                rowData.createCell(1).setCellValue(salaryDetails.getEmployee().getFirstName()+" "+salaryDetails.getEmployee().getLastName());
                rowData.createCell(2).setCellValue(salaryDetails.getEmployeeESICAmount());
                rowData.createCell(3).setCellValue(salaryDetails.getEmployerESICAmount());
                rowData.createCell(4).setCellValue(salaryDetails.getEmployeePFAmount());
                rowData.createCell(5).setCellValue(salaryDetails.getEmployerPFAmount());
                rowData.createCell(6).setCellValue(salaryDetails.getMedicalInsurance());
                rowData.createCell(7).setCellValue(salaryDetails.getGrossSalary());
                rowData.createCell(8).setCellValue(salaryDetails.getMonth());
                rowData.createCell(9).setCellValue(salaryDetails.getAdhoc());
                rowData.createCell(10).setCellValue(salaryDetails.getCreditedDate());
                rowData.createCell(11).setCellValue(list1.get().getAccountNumber());
                rowData.createCell(12).setCellValue(list1.get().getBankName());
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            workbook.close();
            out.close();
        }
    }
}
