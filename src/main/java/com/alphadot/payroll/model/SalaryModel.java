package com.alphadot.payroll.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "salary_table")
public class SalaryModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="serial_id")
	private int serialNo;
	
	@Column(name="emp_id")
	private int empId;
	
	@Column(name="month")
	private String month;
	
	@Column(name="name")
	private String name;
	
	@Column(name="year")
	private String year;
	
	@Column(name="leave_counts")
	private int leaveCounts;
	
	@Column(name="leave_dates")
	private String leavedates;
	
	@Column(name="days_worked")
	private int workedDays;
	
	@Column(name="total_working_days")
	private int totalWorkingDays;
}