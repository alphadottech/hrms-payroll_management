package com.adt.payroll.dto;

import lombok.Data;

@Data
public class TimesheetRequest {

	private int employeeId;

	private String checkOut;

	private String checkIn;

	private String date;

}
