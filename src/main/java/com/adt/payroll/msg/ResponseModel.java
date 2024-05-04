package com.adt.payroll.msg;

import lombok.Data;

@Data
public class ResponseModel {

	private int employeeId;

	private String checkOut;

	private String checkIn;

	private String workingHour;

	private String date;

	private String status;

}

