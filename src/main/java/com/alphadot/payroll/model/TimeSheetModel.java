package com.alphadot.payroll.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;


@Entity
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "Time_sheet")
@Data
public class TimeSheetModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="timeSheet_id",columnDefinition = "serial")
	private int timeSheetId;
	
	@Column(name = "employee_id")
	 private int employeeId; 
		
	@Column(name = "checkOut")
	    private String checkOut;
	
	@Column(name = "checkIn")
	    private String checkIn;
	
	@Column(name = "workingHour")
	    private String workingHour;
	
	@Column(name = "date")
	    private String date;

	@Column(name="status")
	private String status;

	@Column(name="month")
	private String month;

	@Column(name="year")
	private String year;

	public int getTimeSheetId() {
		return timeSheetId;
	}

	public void setTimeSheetId(int timeSheetId) {
		this.timeSheetId = timeSheetId;
	}

	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}

	public String getCheckOut() {
		return checkOut;
	}

	public void setCheckOut(String checkOut) {
		this.checkOut = checkOut;
	}

	public String getCheckIn() {
		return checkIn;
	}

	public void setCheckIn(String checkIn) {
		this.checkIn = checkIn;
	}

	public String getWorkingHour() {
		return workingHour;
	}

	public void setWorkingHour(String workingHour) {
		this.workingHour = workingHour;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public TimeSheetModel(int timeSheetId, int employeeId, String checkOut, String checkIn, String workingHour,
			String date, String status, String month, String year) {
		super();
		this.timeSheetId = timeSheetId;
		this.employeeId = employeeId;
		this.checkOut = checkOut;
		this.checkIn = checkIn;
		this.workingHour = workingHour;
		this.date = date;
		this.status = status;
		this.month = month;
		this.year = year;
	}

	public TimeSheetModel() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "TimeSheetModel [timeSheetId=" + timeSheetId + ", employeeId=" + employeeId + ", checkOut=" + checkOut
				+ ", checkIn=" + checkIn + ", workingHour=" + workingHour + ", date=" + date + ", status=" + status
				+ ", month=" + month + ", year=" + year + "]";
	}
	
	
	
}
