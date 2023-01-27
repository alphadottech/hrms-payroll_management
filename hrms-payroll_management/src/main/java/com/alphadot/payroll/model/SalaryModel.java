package com.alphadot.payroll.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="salary_table")
public class SalaryModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="serial_id",columnDefinition = "serial")
	private int serialNo;
	
	@Column(name="emp_id")
	private int empId;
	
	@Column(name="month")
	private int month;
	
	@Column(name="year")
	private int year;
	
	@Column(name="leave_dates")
	private String leavedates;
	
	@Column(name="working_days")
	private int workingDay;
}
