package com.adt.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewPaySlipDto {

	private String empName;
	private String designation;
	private String creditedDate;
	private String accountNo;
	private String payPeriods;
	private Integer empTotalWorkingDays;
	private Integer officeTotalWorkingDays;
	private Integer leavesTaken;
	private Double adhoc;
	private Double grossSalary;
	private Double netAmountPayable;

}
