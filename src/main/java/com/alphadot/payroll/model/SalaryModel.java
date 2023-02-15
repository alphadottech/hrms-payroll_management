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
@Table(name="salary_table")
public class SalaryModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="serial_id")
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
