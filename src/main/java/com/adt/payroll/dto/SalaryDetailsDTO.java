package com.adt.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryDetailsDTO {

	private int empId;
	private Double basic;
	private Double houseRentAllowance;
	private Double employeeESICAmount;
	private Double employerESICAmount;
	private Double employeePFAmount;
	private Double employerPFAmount;
	private Double grossSalary;
	private Double netSalary;
	private Double medicalInsurance;
	public int getEmpId() {
		return empId;
	}
	public void setEmpId(int empId) {
		this.empId = empId;
	}
	public Double getBasic() {
		return basic;
	}
	public void setBasic(Double basic) {
		this.basic = basic;
	}
	public Double getHouseRentAllowance() {
		return houseRentAllowance;
	}
	public void setHouseRentAllowance(Double houseRentAllowance) {
		this.houseRentAllowance = houseRentAllowance;
	}
	public Double getEmployeeESICAmount() {
		return employeeESICAmount;
	}
	public void setEmployeeESICAmount(Double employeeESICAmount) {
		this.employeeESICAmount = employeeESICAmount;
	}
	public Double getEmployerESICAmount() {
		return employerESICAmount;
	}
	public void setEmployerESICAmount(Double employerESICAmount) {
		this.employerESICAmount = employerESICAmount;
	}
	public Double getEmployeePFAmount() {
		return employeePFAmount;
	}
	public void setEmployeePFAmount(Double employeePFAmount) {
		this.employeePFAmount = employeePFAmount;
	}
	public Double getEmployerPFAmount() {
		return employerPFAmount;
	}
	public void setEmployerPFAmount(Double employerPFAmount) {
		this.employerPFAmount = employerPFAmount;
	}
	public Double getGrossSalary() {
		return grossSalary;
	}
	public void setGrossSalary(Double grossSalary) {
		this.grossSalary = grossSalary;
	}
	public Double getNetSalary() {
		return netSalary;
	}
	public void setNetSalary(Double netSalary) {
		this.netSalary = netSalary;
	}
	public Double getMedicalInsurance() {
		return medicalInsurance;
	}
	public void setMedicalInsurance(Double medicalInsurance) {
		this.medicalInsurance = medicalInsurance;
	}
	
	

}
