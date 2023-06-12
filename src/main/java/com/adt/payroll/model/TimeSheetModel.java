package com.adt.payroll.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

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

	@Column(name="leaveInterval")
	private String leaveInterval;

	@Column(name="intervalStatus")
	private Boolean intervalStatus;

	@Transient
    private String employeeName;
	
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
