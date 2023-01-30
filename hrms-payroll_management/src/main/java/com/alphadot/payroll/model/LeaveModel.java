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
@Table(name="leave_balance")
public class LeaveModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="serial_id",columnDefinition = "serial")
	private int serialId;
	
	@Column(name="employee_id")
	private int empId;
	
	@Column(name="leave_balance")
	private int leaveBalance;
}
