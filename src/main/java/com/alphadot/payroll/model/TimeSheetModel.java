package com.alphadot.payroll.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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
