package com.adt.payroll.model.payload;


import lombok.Data;

@Data
public class PriorTimeManagementRequest {
	private int employeeId;
	
	private String email;
	
	private String checkOut;
	
	private String checkIn;
	
	private String date;
	
	private String status;
	

}
