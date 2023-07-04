package com.adt.payroll.model;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "leave_balance")
public class LeaveModel {

	@Id
	@Column(name="empid")
	private int empId;
	
	@Column(name="name")
	private String name;


	@Column(name="leave_balance")
	private int leaveBalance;





	
}
