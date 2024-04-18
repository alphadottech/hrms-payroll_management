package com.adt.payroll.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.adt.payroll.dto.CurrentDateTime;

@Component
public class Util {
    @Value("${time.zone}")
    private String timezone;
    
    @Value("${holiday}")
    private String[] holiday;

    public static final String ADJUSTMENT = "ADJUSTMENT";
    public static final String TDS = "TDS";
    public static final String MEDICAL_INSURANCE = "MEDICAL INSURANCE";
    public static final String ADDRESS = "AlphaDot Technologies Pvt Ltd\nMPSEDC STP Building\n19A Electronic Complex,\nPardeshipura,Indore (M.P) 452003";
    public static final String Name = "Name";

    public static final String DESIGNATION = "DESIGNATION";
    public static final String AccountNumber = "ACCOUNT NUMBER";
    public static final String BankName = "BANK NAME";
    public static final String Gmail = "Gmail";
    public static final String Basic = "Basic";
    public static final String Hra = "Hra";
    public static final String NumberOfLeavesTaken = "Number of Leaves Taken";
    public static final String GrossSalary = "Gross Salary";
    public static final String PF = "PF";
    public static final String salary = "Salary";
    public static final String PayPeriods = "Pay Periods";

    public static final String PaidLeave = "Paid Leave";
    public static final String NetAmountPayable = "Net amount payable";
    public static final String NetAmount = "Net Amount";
    public static final String YourWorkingDays = "Present";
    public static final String TotalWorkingDays = "Working day";

    public static final String Esic = "Esic";
    public static final String Leave = "Unpaid Leave";
    public static final String AmountDeductedForLeaves = "Amount deducted for leaves";
    public static final String AmountPayablePerDay = "Amount Payable per day";
    public static final String PaySlip = "Pay Slip";
    public static final String EmployeeInformation = "Employee Information";
    public static final String Gender = "Gender";
    public static final String EmployeeNumber = "Employee Id";
    
    public static final String ADDRESS2 = "MPSEDC STP Building ,19A Electronic Complex,\n" +
            "Pardeshipura,Indore (M.P) 452003";

    public static final String HalfDay = "Half Day";
    public static final String JobTitle = "Job Title";
    public static final String JoiningDate = "Joining Date";

    public static String months[] = {"JANUARY", "FEBRUARY", "MARCH", "APRIL",
            "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER",
            "OCTOBER", "NOVEMBER", "DECEMBER"};
    public static final String Date = "Date";
    public static final String MobileNo = "Mobile No";
    public static final String StatusPresent = "Present";
    public static final String Adhoc = "ADHOC";

    public static final String Adhoc1 = "ADHOC1";

    public static final String Adhoc2 = "ADHOC2";

    public static final String Adhoc3 = "ADHOC3";
    public static final int SaturdyaValue = 2;
    public static final String msg = "Dear [Name],\r\n"
            + "\r\n"
            + "I hope this email finds you well. I am writing to share your payslip for the month of [Month, Year].\r\n"
            + "\r\n"
            + "Please find attached a copy of your payslip, which includes details of your salary, deductions, and net pay for the month. If you have any questions or concerns about your payslip, please do not hesitate to contact me.\r\n"
            + "\r\n"
            + "Thank you for your hard work and dedication to the company. We appreciate your contributions and look forward to working with you.\r\n"
            + "\r\n"
            + "Best regards,\r\n"

            + "[Your Name]";
    
    public static final String MESSAGE="The salary slip details for [Name] has been entered incorrectly. \r\n Kindy fill correct information !! \r\n"
    		+ "\r\n"
            + "Regards,"
            + "\r\n"
            +"AlphaDot Technologies";

    public CurrentDateTime getDateTime() {
        TimeZone timeZone = TimeZone.getTimeZone(timezone);
// Create a calendar object with the current date and time in the Kolkata time zone
        Calendar calendar = Calendar.getInstance(timeZone);
// Get the year, month, day, hour, minute, and second from the calendar object
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Note: Month is zero-based, so add 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String formattedDate = dateFormat.format(calendar.getTime());
        String formattedTime = timeFormat.format(calendar.getTime());

        CurrentDateTime currentDateTime = new CurrentDateTime();
        currentDateTime.setYear(year);
        currentDateTime.setMonth(month);
        currentDateTime.setDay(day);
        currentDateTime.setHour(hour);
        currentDateTime.setMinute(minute);
        currentDateTime.setSecond(second);
        currentDateTime.setCurrentDate(formattedDate);
        currentDateTime.setCurrentTime(formattedTime);
        return currentDateTime;
    }

	public int getWorkingDays() throws ParseException, IOException {
		int monthd;
		Date date = new Date();
		int years = date.getYear();
		int currentYear = years + 1900;
		monthd = date.getMonth();
		if (monthd == 1) {
			monthd = monthd = 12;
			currentYear = currentYear - 1;
		}
		String year = "" + currentYear;
		String month = Month.of(monthd).name();
		List<String> holidays = Arrays.asList(holiday);
		List<String> lists = new ArrayList<>();
		SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
		SimpleDateFormat outputFormat = new SimpleDateFormat("MM");
		Calendar cal = Calendar.getInstance();
		cal.setTime(inputFormat.parse(month));
		int workDays;
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
		workDays = workDays - Util.SaturdyaValue;
		return workDays;
	}
	
	
	public static ImageData getImage() throws IOException {
		ImageData datas = null;
		Resource resource = new ClassPathResource("image/alphadot_tech_logo.jpg");
		try (InputStream inputStream = resource.getInputStream();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			return datas = ImageDataFactory.create(outputStream.toByteArray());
		}

	}

}