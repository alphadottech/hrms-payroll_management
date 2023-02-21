package com.alphadot.payroll.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity

@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "Leave_Time")
public class LeaveTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private int empId;
	private String date;
	private String month;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getEmpId() {
		return empId;
	}
	public void setEmpId(int empId) {
		this.empId = empId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public LeaveTime(int id, int empId, String date, String month) {
		super();
		this.id = id;
		this.empId = empId;
		this.date = date;
		this.month = month;
	}
	public LeaveTime() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "LeaveTime [id=" + id + ", empId=" + empId + ", date=" + date + ", month=" + month + "]";
	}
	
	
	

	
}
