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
	private double grossDeduction;
	private Integer paidLeave;
	private Integer unpaidLeave;
	private Integer halfday;
	private double leaveDeductionAmount;
	private double netSalaryAmount;
	private double salary;

	public PaySlip(Integer empId, String name, String jobTitle, String presentDate, String bankName,
			String accountNumber, String payPeriods, Integer youWorkingDays, Integer totalWorkingDays,
			Integer numberOfLeavesTaken, Float amountDeductedForLeaves, Float grossSalary, Float netAmountPayable,
			Integer adhoc) {
		super();
		this.empId = empId;
		Name = name;
		this.jobTitle = jobTitle;
		this.presentDate = presentDate;
		this.bankName = bankName;
		this.accountNumber = accountNumber;
		this.payPeriods = payPeriods;
		this.youWorkingDays = youWorkingDays;
		this.totalWorkingDays = totalWorkingDays;
		this.numberOfLeavesTaken = numberOfLeavesTaken;
		this.amountDeductedForLeaves = amountDeductedForLeaves;
		this.grossSalary = grossSalary;
		this.netAmountPayable = netAmountPayable;
		this.adhoc = adhoc;
	}

}
