package com.adt.payroll.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryDetailsDTO {

	private int empId;
	private Double salary;
	private String bankName;
	private String designation;
	private String joinDate;
	private String accountNumber;
	private String ifscCode;
	private Double basic;
	private Double houseRentAllowance;
	private Double employeeESICAmount;
	private Double employerESICAmount;
	private Double employeePFAmount;
	private Double employerPFAmount;
	private Double grossSalary;
	private Double netSalary;
	private Double medicalInsurance;
	private boolean onlyBasic;
	private Double variableAmount;

}
