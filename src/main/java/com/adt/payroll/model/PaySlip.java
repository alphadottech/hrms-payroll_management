package com.adt.payroll.model;

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
    private String presentDate;
	private String bankName;
	private String accountNumber;
	private String payPeriods;
	private Integer youWorkingDays;
	private Integer totalWorkingDays;
	private Integer numberOfLeavesTaken;
	private Float amountDeductedForLeaves;
    private Float grossSalary;
	private Float netAmountPayable;
	private Integer adhoc;

}
