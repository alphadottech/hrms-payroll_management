package com.adt.payroll.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(catalog = "hrms_sit", schema = "payroll_schema", name = "salary_details")
public class SalaryDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "basic")
	private Double basic;

	@Column(name = "employee_esic_amount")
	private Double employeeESICAmount;

	@Column(name = "employer_esic_amount")
	private Double employerESICAmount;

	@Column(name = "employee_pf_amount")
	private Double employeePFAmount;

	@Column(name = "employer_pf_amount")
	private Double employerPFAmount;

	@Column(name = "medical_insurance")
	private Double medicalInsurance;

	@Column(name = "tds")
	private Double tds;

	@Column(name = "gross_salary")
	private Double grossSalary;

	@Column(name = "net_salary")
	private Double netSalary;

	@Column(name = "adhoc")
	private Double adhoc;

	@Column(name = "adjustment")
	private Double adjustment;

	@Column(name = "hra")
	private Double houseRentAllowance;

	@Column(name = "da")
	private Double dearnessAllowance;

	@Column(name = "income_tax")
	private Double incomeTax;
	
	@Column(name = "absent_deduction")
	private Double absentDeduction;

	@OneToOne
	@JoinColumn(name = "empId", referencedColumnName = "EMPLOYEE_ID", nullable = false, insertable = false, updatable = false)
	private User employee;
	private int empId;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Double getBasic() {
		return basic;
	}
	public void setBasic(Double basic) {
		this.basic = basic;
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
	public Double getMedicalInsurance() {
		return medicalInsurance;
	}
	public void setMedicalInsurance(Double medicalInsurance) {
		this.medicalInsurance = medicalInsurance;
	}
	public Double getTds() {
		return tds;
	}
	public void setTds(Double tds) {
		this.tds = tds;
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
	public Double getAdhoc() {
		return adhoc;
	}
	public void setAdhoc(Double adhoc) {
		this.adhoc = adhoc;
	}
	public Double getAdjustment() {
		return adjustment;
	}
	public void setAdjustment(Double adjustment) {
		this.adjustment = adjustment;
	}
	public Double getHouseRentAllowance() {
		return houseRentAllowance;
	}
	public void setHouseRentAllowance(Double houseRentAllowance) {
		this.houseRentAllowance = houseRentAllowance;
	}
	public Double getDearnessAllowance() {
		return dearnessAllowance;
	}
	public void setDearnessAllowance(Double dearnessAllowance) {
		this.dearnessAllowance = dearnessAllowance;
	}
	public Double getIncomeTax() {
		return incomeTax;
	}
	public void setIncomeTax(Double incomeTax) {
		this.incomeTax = incomeTax;
	}
	public Double getAbsentDeduction() {
		return absentDeduction;
	}
	public void setAbsentDeduction(Double absentDeduction) {
		this.absentDeduction = absentDeduction;
	}
	public User getEmployee() {
		return employee;
	}
	public void setEmployee(User employee) {
		this.employee = employee;
	}
	public int getEmpId() {
		return empId;
	}
	public void setEmpId(int empId) {
		this.empId = empId;
	}
	
	
}
