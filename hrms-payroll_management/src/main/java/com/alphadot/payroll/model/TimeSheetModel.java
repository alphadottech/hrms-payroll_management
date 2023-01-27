package com.alphadot.payroll.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Time_sheet")
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
}
