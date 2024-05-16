package com.adt.payroll.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "leave_balance")
public class LeaveBalance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "emp_id")
	private int empId;

	@Column(name = "name")
	private String name;

	@Column(name = "leave_balance")
	private int leaveBalance;
	
//ritika-----------
	@Column(name = "half_day")
	private int halfDay;

	@Column(name = "paid_leave")
	private int paidLeave;
	
	@Column(name = "unpaid_leave")
	private int unpaidLeave;
//ritika-----------

	
}
