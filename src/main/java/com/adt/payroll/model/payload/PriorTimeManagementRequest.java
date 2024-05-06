package com.adt.payroll.model.payload;

import lombok.Data;

@Data
public class PriorTimeManagementRequest {
	int employeeId;
	String email;
	String checkOut;
	String checkIn;
	String date;
	String workingHour;
	String status;

}
