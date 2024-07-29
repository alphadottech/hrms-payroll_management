package com.adt.payroll.model;

import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(catalog = "hrms_sit", schema = "payroll_schema", name = "monthly_salary_details")
public class
MonthlySalaryDetails {

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

	@Column(name = "gross_deduction")
	private Double grossDeduction;

	@Column(name = "absent_deduction")
	private Double absentDeduction;

	@Column(name = "salary_credited_date")
	private String creditedDate;

	@Column(name = "salary_month")
	private String month;

	@Column(name = "bonus")
	private Double bonus;

	@Column(name = "present_days")
	private Integer presentDays;

	@Column(name = "absent_leaves")
	private Integer absentDays;

	@Column(name = "total_working_days")
	private Integer totalWorkingDays;

	@Column(name = "half_day")
	private Integer halfDay;

	@Column(name = "paid_leave")
	private Integer paidLeave;

	@Column(name = "unpaid_leave")
	private Integer unpaidLeave;

	@OneToOne
	@JoinColumn(name = "empId", referencedColumnName = "EMPLOYEE_ID", nullable = false, insertable = false, updatable = false)
	private User employee;
	private int empId;

	@Column(name = "is_active")
	private boolean isActive;

	@Column(name = "updated_When")
	private Timestamp updatedWhen;
	
	@Column(name = "comments")
	private String comment;
}
