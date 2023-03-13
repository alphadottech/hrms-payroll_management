package com.alphadot.payroll.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
	private float amountDeductedForLeaves;
	private float amountPayablePerDay;
	private float grossSalary;
	private float netAmountPayable;

}
