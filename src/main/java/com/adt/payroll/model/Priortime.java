package com.adt.payroll.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "priortime_table")
@Data
public class Priortime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "priortime_id", columnDefinition = "serial")
	private int priortimeId;

	@Column(name = "employee_id")
	private int employeeId;

	@Column(name = "checkIn")
	private String checkIn;

	@Column(name = "checkOut")
	private String checkOut;

	@Column(name = "date")
	private String date;

	@Column(name = "month")
	private String month;

	@Column(name = "status")
	private String status;

	@Column(name = "workingHour")
	private String workingHour;

	@Column(name = "year")
	private String year;

	@Column(name = "email")
	private String email;

}
