package com.adt.payroll.service;

import com.adt.payroll.model.*;
import com.adt.payroll.repository.ImageRepo;
import com.adt.payroll.repository.PayRecordRepo;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.repository.UserRepo;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.EntityNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayRollServiceImpl implements PayRollService {

    private static final Logger log = LogManager.getLogger(PayRollServiceImpl.class);
   
    @Autowired
    private JavaMailSender javaMailSender;

//    @Value("${spring.mail.username}")
//    private String sender;
    
    @Autowired
    private TimeSheetRepo timeSheetRepo;

    @Autowired
    private PayRecordRepo payRecordRepo;
    @Autowired
    private TableDataExtractor dataExtractor;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ImageRepo imgRepo;

    @Value("${holiday}")
    private String[] holiday;
    
    @Autowired
    private CommonEmailService mailService;

    public PaySlip createPaySlip(int empId, String month, String year)
            throws ParseException, IOException {
        log.info("inside method");
        String submitDate = "", status = "", employee_id = "";
        String monthYear = month + " " + year;
        int yourWorkingDays = 0, leaves = 0, workDays = 0, saturday = Util.SaturdyaValue, adhoc = 0;
        LocalDate currentdate = LocalDate.now();
        PaySlip paySlip = new PaySlip();
        String sql = "select * from employee_schema.employee_expenses";
        List<Map<String, Object>> tableData = dataExtractor.extractDataFromTable(sql);


        List<String> holidays = Arrays.asList(holiday);
        List<String> lists = new ArrayList<>();

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
                .filter(x -> x.getWorkingHour() == null && (x.getCheckIn() == null && x.getStatus().equalsIgnoreCase("Leave")))
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

        for (Map<String, Object> expense : tableData) {
            submitDate = String.valueOf(expense.get("payment_date")).substring(3, 5);
            status = String.valueOf(expense.get("status"));
            employee_id = String.valueOf(expense.get("employee_id"));
            if (submitDate.equals(monthDate) && status.equals("Accepted") && employee_id.equalsIgnoreCase(String.valueOf(empId))) {
                adhoc += Integer.parseInt(String.valueOf(expense.get("expense_amount")));
            }
        }

        float grossSalary = (int) user.get().getSalary();
        int totalWorkingDays = workDays - saturday;
        float amountPerDay = grossSalary / totalWorkingDays;
        float leavePerDay = leaves * amountPerDay;
        float netAmount = (yourWorkingDays * amountPerDay);
        netAmount += adhoc;
        paySlip = new PaySlip(empId, name, user.get().getDesignation(),
                dtf.format(currentdate), user.get().getBankName(), user.get().getAccountNumber(),
                firstDayMonth + " - " + lastDayOfMonth, yourWorkingDays, totalWorkingDays, leaves, leavePerDay,
                grossSalary, netAmount, adhoc);
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
        float columnWidth[] = {col, col};
        Table table = new Table(columnWidth);
        table.setBackgroundColor(new DeviceRgb(63, 169, 219)).setFontColor(Color.WHITE);
        table.addCell(new Cell().add("Pay Slip").setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE).setMarginTop(30f).setMarginBottom(30f).setFontSize(30f)
                .setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(Util.ADDRESS).setTextAlignment(TextAlignment.RIGHT).setMarginTop(30f)
                .setMarginBottom(30f).setBorder(Border.NO_BORDER).setMarginRight(10f));
        float colWidth[] = {150, 150, 100, 100};
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
        itemInfo.addCell(new Cell().add(Util.GrossSalary));
        itemInfo.addCell(new Cell().add(String.valueOf(grossSalary)));
        itemInfo.addCell(new Cell().add(Util.NetAmountPayable));
        itemInfo.addCell(new Cell().add(String.valueOf(netAmount)));
        document.add(alpha);

        document.add(table);
        document.add(new Paragraph("\n"));
        document.add(employeeTable);
        document.add(itemInfo);
        document.add(new Paragraph("\n(Note - This is a computer generated statement and does not require a signature.)").setTextAlignment(TextAlignment.CENTER));
        document.close();
        log.warn("Successfully");

        //sendEmail(baos, name, user.get().getEmail(), monthYear);
        
        mailService.sendEmail(baos, name, user.get().getEmail(), monthYear);
        
        return paySlip;
    }

    // Excel Pay Slip

    public String generatePaySlip(MultipartFile file) throws IOException, ParseException {
        String empId = "", name = "", salary = "",
                paidLeave = "", bankName = "", accountNumber = "", gmail = "", designation = "", submitDate = "", status = "", employee_id = "", joiningDate = "";
        String sheetName = "";
        int adhoc = 0, adhoc1 = 0, adhoc2 = 0, adhoc3 = 0, workingDays = 0, present = 0, leave = 0, halfDay = 0, limit = 30;
        Map<String, Integer> excelColumnName = new HashMap<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM");
        NumberFormat format = NumberFormat.getInstance();
        String projDir = System.getProperty("user.dir");
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        DataFormatter dataFormatter = new DataFormatter();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sh = workbook.getSheetAt(i);
            if (sh.getLastRowNum() > 0) {
                sheetName = sh.getSheetName();
            }
        }

        XSSFSheet sheet = workbook.getSheet(sheetName);

        Row headerRow = sheet.getRow(0);

        int columnCount = headerRow.getLastCellNum();
        String columnHeader = "";
        for (int i = 0; i < columnCount; i++) {
            headerRow.getCell(i);
            headerRow.getCell(i).getStringCellValue();
            columnHeader = String.valueOf(headerRow.getCell(i)).trim();

            excelColumnName.put(columnHeader, i);
        }

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
        String sql = "select * from employee_schema.employee_expenses";
        List<Map<String, Object>> tableData = dataExtractor.extractDataFromTable(sql);


        for (int i = 2; i <= 50; i++) {
            try {
                XSSFRow row = sheet.getRow(i);
                try {
                    empId = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.EmployeeNumber)));
                    name = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Name)));
                    workingDays = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.TotalWorkingDays))));
                    present = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.YourWorkingDays))));
                    leave = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Leave))));
                    halfDay = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.HalfDay))));
                    salary = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.salary)));
                    paidLeave = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.PaidLeave)));
                    bankName = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.BankName)));
                    accountNumber = format.format(row.getCell(excelColumnName.get(Util.AccountNumber)).getNumericCellValue()).replace(",", "");
                    designation = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.DESIGNATION)));
                    gmail = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Gmail)));
//                    adhoc= Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc))));
                    joiningDate = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.JoiningDate)));
                    adhoc1 = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc1))));
                    adhoc2 = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc2))));
                    adhoc3 = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc3))));

                    if (halfDay > limit || leave > limit || workingDays > limit || present > limit) {
                        continue;
                    }
                    for (Map<String, Object> expense : tableData) {
                        submitDate = String.valueOf(expense.get("payment_date")).substring(3, 5);
                        status = String.valueOf(expense.get("status"));
                        employee_id = String.valueOf(expense.get("employee_id"));
                        if (submitDate.equals(monthDate) && status.equals("Accepted") && employee_id.equalsIgnoreCase(String.valueOf(empId))) {
                            adhoc += Integer.parseInt(String.valueOf(expense.get("expense_amount")));
                        }
                    }


                    baos = createPdf(empId, name, workingDays, present, leave, halfDay, salary,
                            paidLeave, date, bankName, accountNumber, designation, joiningDate, adhoc, adhoc1, adhoc2, adhoc3, payPeriod);


//                    sendEmail(baos, name, gmail, monthYear);
                    
                    mailService.sendEmail(baos, name, gmail, monthYear);
                    
                } catch (Exception e) {
                    continue;
                }
            } catch (Exception e) {
                break;
            }
        }
        return "Mail Send Successfully";
    }


    public ByteArrayOutputStream createPdf(String empId, String name, int totalWorkingDays, int present,
                                           int leave, int halfDay, String salary, String paidLeave, String date, String bankName,
                                           String accountNumber, String designation, String joiningDate, int adhoc, int adhoc1, int adhoc2, int adhoc3, String payPeriod) throws  IOException {

        float grossSalary = Float.valueOf(salary);
//        int totalWorkingDays = Integer.parseInt(totalworkingDays);
//        int leaves = Integer.parseInt(leave) - Integer.parseInt(paidLeave);
        int yourWorkingDays = present + Integer.parseInt(paidLeave);

        float amountPerDay = grossSalary / totalWorkingDays;

        float HalfDays = halfDay * amountPerDay / 2;

        float netAmount = (yourWorkingDays * amountPerDay) - HalfDays;

        netAmount = netAmount + adhoc + adhoc1 + adhoc2 + adhoc3;
        if (netAmount < 0) {
            netAmount = 0;
            adhoc = 0;
        }
        String a = String.valueOf(adhoc), b = String.valueOf(adhoc1), c = String.valueOf(adhoc2), d = String.valueOf(adhoc3);
        String adHoc = a.contains("-") ? a.replace("-", "") : a;
        String adHoc1 = b.contains("-") ? b.replace("-", "") : b;
        String adHoc2 = c.contains("-") ? c.replace("-", "") : c;
        String adHoc3 = d.contains("-") ? d.replace("-", "") : d;

        ImageData datas = ImageDataFactory.create(imgRepo.search());
        log.info("image path set");
        Image alpha = new Image(datas);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(baos);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);
        pdfDocument.setDefaultPageSize(PageSize.A4);

        float col = 250f;
        float columnWidth[] = {col, col};
        Table table = new Table(columnWidth);
        table.setBackgroundColor(new DeviceRgb(63, 169, 219)).setFontColor(Color.WHITE);
        table.addCell(new Cell().add("Pay Slip").setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE).setMarginTop(30f).setMarginBottom(30f).setFontSize(30f)
                .setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(Util.ADDRESS).setTextAlignment(TextAlignment.RIGHT).setMarginTop(30f)
                .setMarginBottom(30f).setBorder(Border.NO_BORDER).setMarginRight(10f));
        float colWidth[] = {125, 150, 125, 100};
        Table employeeTable = new Table(colWidth);
        employeeTable.addCell(new Cell(0, 4).add(Util.EmployeeInformation + "                                                                          " + "Date : " + date).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(Util.EmployeeNumber).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(empId).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(Util.JoiningDate).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(joiningDate).setBorder(Border.NO_BORDER));
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
        itemInfo.addCell(new Cell().add(String.valueOf(present)));
        itemInfo.addCell(new Cell().add(Util.TotalWorkingDays));
        itemInfo.addCell(new Cell().add(String.valueOf(totalWorkingDays)));
        itemInfo.addCell(new Cell().add(Util.NumberOfLeavesTaken));
        itemInfo.addCell(new Cell().add(String.valueOf(leave)));
        itemInfo.addCell(new Cell().add("Paid Leave"));
        itemInfo.addCell(new Cell().add(paidLeave));
        itemInfo.addCell(new Cell().add(Util.Adhoc));
        itemInfo.addCell(new Cell().add(adHoc));
        itemInfo.addCell(new Cell().add(Util.Adhoc1));
        itemInfo.addCell(new Cell().add(adHoc1));
        itemInfo.addCell(new Cell().add(Util.Adhoc2));
        itemInfo.addCell(new Cell().add(adHoc2));
        itemInfo.addCell(new Cell().add(Util.Adhoc3));
        itemInfo.addCell(new Cell().add(adHoc3));
        itemInfo.addCell(new Cell().add(Util.GrossSalary));
        itemInfo.addCell(new Cell().add(String.valueOf(salary)));
        itemInfo.addCell(new Cell().add(Util.NetAmountPayable));
        itemInfo.addCell(new Cell().add(String.format("%.2f", netAmount)));
        document.add(alpha);
        document.add(table);
        document.add(new Paragraph("\n"));
        document.add(employeeTable);
        document.add(itemInfo);
        document.add(new Paragraph("\n(Note - This is a computer generated statement and does not require a signature.)").setTextAlignment(TextAlignment.CENTER));
        document.close();

        log.info("Successfully");
        return baos;
    }

//    public void sendEmail(ByteArrayOutputStream baos, String name, String gmail, String monthYear) {
//        String massage = Util.msg.replace("[Name]", name).replace("[Your Name]", "AlphaDot Technologies")
//                .replace("[Month, Year]", monthYear);
//
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//        MimeMessageHelper mimeMessageHelper;
//
//        try {
//
//            DataSource source = new ByteArrayDataSource(baos.toByteArray(), "application/octet-stream");
//            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
//            mimeMessageHelper.setFrom(sender);
//            mimeMessageHelper.setTo(gmail);
//            mimeMessageHelper.setText(massage);
//            mimeMessageHelper.setSubject("Salary Slip" + "-" + monthYear);
//            mimeMessageHelper.addAttachment(name + ".pdf", source);
//
//            javaMailSender.send(mimeMessage);
//
//            log.info("Mail send Successfully");
//        } catch (MessagingException e) {
//            log.info("Error");
//
//        }
//
//    }


    @Override
    public byte[] viewPay(SalaryModel salaryModel, String month, String year) throws ParseException, UnsupportedEncodingException {
        log.info("inside method");
        int empId = salaryModel.getEmpId();
        PayRecord payRecords = payRecordRepo.findByEmpId(empId);
        if(payRecords!=null){
            if(payRecords.getEmpId() == empId && payRecords.getMonth().equalsIgnoreCase(month) && payRecords.getYear().equalsIgnoreCase(year))
            return payRecords.getPdf();
        }
        String submitDate = "", status = "", employee_id = "";
        String monthYear = month + " " + year;
        int yourWorkingDays = 0, leaves = 0, workDays = 0, saturday = Util.SaturdyaValue, adhoc = 0;
        LocalDate currentdate = LocalDate.now();

        String sql = "select * from employee_schema.employee_expenses";
        List<Map<String, Object>> tableData = dataExtractor.extractDataFromTable(sql);

        PayRecord payRecord = new PayRecord();
        payRecord.setEmpId(empId);
        payRecord.setMonth(month);
        payRecord.setEmpName(salaryModel.getEmpName());
        payRecord.setYear(year);

        List<String> holidays = Arrays.asList(holiday);
        List<String> lists = new ArrayList<>();

        SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM"); // 01-12

        Calendar cal = Calendar.getInstance();
        cal.setTime(inputFormat.parse(month));

        Optional<User> user = Optional.ofNullable(userRepo.findById(empId)
                .orElseThrow(() -> new EntityNotFoundException("employee not found :" + empId)));
        String name = salaryModel.getEmpName();
        List<TimeSheetModel> timeSheetModel = timeSheetRepo.search(empId, month.toUpperCase(), year);

        yourWorkingDays = timeSheetModel.stream()
                .filter(x -> x.getWorkingHour() != null && x.getStatus().equalsIgnoreCase(Util.StatusPresent))
                .collect(Collectors.toList()).size();
        leaves = timeSheetModel.stream()
                .filter(x -> x.getWorkingHour() == null && (x.getCheckIn() == null && x.getStatus().equalsIgnoreCase("Leave")))
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

        for (Map<String, Object> expense : tableData) {
            submitDate = String.valueOf(expense.get("payment_date")).substring(3, 5);
            status = String.valueOf(expense.get("status"));
            employee_id = String.valueOf(expense.get("employee_id"));
            if (submitDate.equals(monthDate) && status.equals("Accepted") && employee_id.equalsIgnoreCase(String.valueOf(empId))) {
                adhoc += Integer.parseInt(String.valueOf(expense.get("expense_amount")));
            }
        }

        float grossSalary = salaryModel.getSalary();
        int totalWorkingDays = workDays - saturday;
        float amountPerDay = grossSalary / totalWorkingDays;
        float leavePerDay = leaves * amountPerDay;
        float netAmount = (yourWorkingDays * amountPerDay);
        netAmount += adhoc;

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
        float columnWidth[] = {col, col};
        Table table = new Table(columnWidth);
        table.setBackgroundColor(new DeviceRgb(63, 169, 219)).setFontColor(Color.WHITE);
        table.addCell(new Cell().add("Pay Slip").setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE).setMarginTop(30f).setMarginBottom(30f).setFontSize(30f)
                .setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(Util.ADDRESS).setTextAlignment(TextAlignment.RIGHT).setMarginTop(30f)
                .setMarginBottom(30f).setBorder(Border.NO_BORDER).setMarginRight(10f));
        float colWidth[] = {125, 150, 125, 100};
        Table employeeTable = new Table(colWidth);
        employeeTable.addCell(new Cell(0, 4).add(Util.EmployeeInformation + "                                                                          " + "Date : " + dtf.format(currentdate)).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(Util.EmployeeNumber).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(String.valueOf(empId)).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(Util.JoiningDate).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(salaryModel.getJoinDate()).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(Util.Name).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(salaryModel.getEmpName()).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(Util.BankName).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(salaryModel.getBankName()).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(Util.JobTitle).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(salaryModel.getRole()).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(Util.AccountNumber).setBorder(Border.NO_BORDER));
        employeeTable.addCell(new Cell().add(salaryModel.getAccountNumber()).setBorder(Border.NO_BORDER));

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
        itemInfo.addCell(new Cell().add(Util.GrossSalary));
        itemInfo.addCell(new Cell().add(String.valueOf(grossSalary)));
        itemInfo.addCell(new Cell().add(Util.NetAmountPayable));
        itemInfo.addCell(new Cell().add(String.valueOf(netAmount)));
        document.add(alpha);

        document.add(table);
        document.add(new Paragraph("\n"));
        document.add(employeeTable);
        document.add(itemInfo);
        document.add(new Paragraph("\n(Note - This is a computer generated statement and does not require a signature.)").setTextAlignment(TextAlignment.CENTER));
        document.close();
        log.warn("Successfully");
        payRecord.setPdf(baos.toByteArray());
        payRecordRepo.save(payRecord);

        return baos.toByteArray();
    }
    @Override
    public String updateNetAmountInExcel(MultipartFile file) throws IOException {

        String salary = "",paidLeave = "",sheetName = "";
        int NetAmount=0, adhoc1 = 0, adhoc2 = 0, adhoc3 = 0, workingDays = 0, present = 0,  halfDay = 0;


        Map<String, Integer> excelColumnName = new HashMap<>();
        String projDir = System.getProperty("user.dir");
        NumberFormat format = NumberFormat.getInstance();
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        DataFormatter dataFormatter = new DataFormatter();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sh = workbook.getSheetAt(i);
            if (sh.getLastRowNum() > 0) {
                sheetName = sh.getSheetName();
            }
        }

        XSSFSheet sheet = workbook.getSheet(sheetName);

        Row headerRow = sheet.getRow(0);

        int columnCount = headerRow.getLastCellNum();
        String columnHeader = "";
        for (int i = 0; i < columnCount; i++) {
            headerRow.getCell(i);
            headerRow.getCell(i).getStringCellValue();
            columnHeader = String.valueOf(headerRow.getCell(i)).trim();

            excelColumnName.put(columnHeader, i);
        }
        for (int i = 2; i <= 50; i++) {
            try {
                XSSFRow row = sheet.getRow(i);
                try {
                    workingDays = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.TotalWorkingDays))));
                    present = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.YourWorkingDays))));
                    halfDay = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.HalfDay))));
                    salary = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.salary)));
                    paidLeave = dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.PaidLeave)));

                    adhoc1 = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc1))));
                    adhoc2 = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc2))));
                    adhoc3 = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(excelColumnName.get(Util.Adhoc3))));
                    double netAmount = calculateNetAmount(workingDays,present,salary,paidLeave,halfDay,adhoc1,adhoc2,adhoc3);
                    row.createCell(excelColumnName.get(Util.NetAmount)).setCellValue(netAmount);

                } catch (Exception e) {
                    continue;
                }
            } catch (Exception e) {
                break;
            }
        }
        FileOutputStream fileOutputStream = new FileOutputStream("C:/Users/hp/Desktop/excel/"+file.getOriginalFilename());
        workbook.write(fileOutputStream);
        fileOutputStream.close();

        return "done";
    }
    public float calculateNetAmount(int totalWorkingDays,int present,String salary,String paidLeave,int halfDay,int adhoc1,int adhoc2,int adhoc3){
        float grossSalary = Float.valueOf(salary);
        int yourWorkingDays = present + Integer.parseInt(paidLeave);

        float amountPerDay = grossSalary / totalWorkingDays;

        float HalfDays = halfDay * amountPerDay / 2;

        float netAmount = (yourWorkingDays * amountPerDay) - HalfDays;

        netAmount = netAmount  + adhoc1 + adhoc2 + adhoc3;
        return netAmount;
    }

}