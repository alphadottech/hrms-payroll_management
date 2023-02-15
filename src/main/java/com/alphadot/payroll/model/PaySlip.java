package com.alphadot.payroll.model;

import lombok.Data;

@Data
public class PaySlip {
	
	private Integer empId;
	private String Name;
	
	private String jobTitle;
	private Long mobileNo;
	private String presentDate;
	private String bankName;
	private String accountNumber;
	private String payPeriods;
	private int youWorkingDays;
	private int totalWorkingDays;
	private int numberOfLeavesTaken;
	private int amountDeductedForLeaves;
	private int amountPayablePerDay;
	private int grossSalary;
	private int netAmountPayable;

}
