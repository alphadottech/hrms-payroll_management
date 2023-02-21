package com.alphadot.payroll.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "leave_balance")
public class LeaveModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="serial_id",columnDefinition = "serial")
	private int serialId;
	
	@Column(name="employee_id")
	private int empId;
	
	@Column(name="leave_balance")
	private int leaveBalance;

	public int getSerialId() {
		return serialId;
	}

	public void setSerialId(int serialId) {
		this.serialId = serialId;
	}

	public int getEmpId() {
		return empId;
	}

	public void setEmpId(int empId) {
		this.empId = empId;
	}

	public int getLeaveBalance() {
		return leaveBalance;
	}

	public void setLeaveBalance(int leaveBalance) {
		this.leaveBalance = leaveBalance;
	}

	public LeaveModel(int serialId, int empId, int leaveBalance) {
		super();
		this.serialId = serialId;
		this.empId = empId;
		this.leaveBalance = leaveBalance;
	}

	public LeaveModel() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "LeaveModel [serialId=" + serialId + ", empId=" + empId + ", leaveBalance=" + leaveBalance + "]";
	}
	
}
