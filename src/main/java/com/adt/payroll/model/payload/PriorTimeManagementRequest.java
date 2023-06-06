package com.adt.payroll.model.payload;

import lombok.Data;

@Data
public class PriorTimeManagementRequest {
	int employeeId;
	String checkOut;
	String checkIn;
	String date;
	String email;
	String month;

}
