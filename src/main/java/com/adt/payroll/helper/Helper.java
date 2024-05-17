package com.adt.payroll.helper;

import com.adt.payroll.model.TimeSheetModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class Helper {
    public static String Headers[]={"employeeId","employeeName","checkOut","checkIn","workingHour","date","status"," month","year","checkInLatitude","checkInLongitude","checkInDistance","checkOutLatitude","checkOutLongitude","checkOutDistance"};
    public static String Sheet_name="Excel_data";

    public static ByteArrayInputStream dataToExcel(List<TimeSheetModel> list) throws IOException {

        //create workbook
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            //create sheet
            Sheet sheet = workbook.createSheet(Sheet_name);
            //create row :Header row
            Row row = sheet.createRow(0);
            for (int i = 0; i < Headers.length; i++) {
                //create cell
                Cell cell = row.createCell(i);
                cell.setCellValue(Headers[i]);
            }
            //value rows
            int rowIndex = 1;
            for (TimeSheetModel ts : list) {
                Row rowData = sheet.createRow(rowIndex);
                rowIndex++;

                rowData.createCell(0).setCellValue(ts.getEmployeeId());
                rowData.createCell(1).setCellValue(ts.getEmployeeName());
                rowData.createCell(2).setCellValue(ts.getCheckOut());
                rowData.createCell(3).setCellValue(ts.getCheckIn());
                rowData.createCell(4).setCellValue(ts.getWorkingHour());
                rowData.createCell(5).setCellValue(ts.getDate());
                rowData.createCell(6).setCellValue(ts.getStatus());
                rowData.createCell(7).setCellValue(ts.getMonth());
                rowData.createCell(8).setCellValue(ts.getYear());
                rowData.createCell(9).setCellValue(ts.getCheckInLatitude());
                rowData.createCell(10).setCellValue(ts.getCheckInLongitude());
                rowData.createCell(11).setCellValue(ts.getCheckInDistance());
                rowData.createCell(12).setCellValue(ts.getCheckOutLatitude());
                rowData.createCell(13).setCellValue(ts.getCheckOutLongitude());
                rowData.createCell(14).setCellValue(ts.getCheckOutDistance());


            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed");
            return null;
        }
        finally {
            workbook.close();
            out.close();
        }

    }
}
