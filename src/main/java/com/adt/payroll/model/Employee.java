package com.adt.payroll.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(catalog = "EmployeeDB", schema = "employee_schema", name = "EmployeeDetails")
public class Employee {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "EmpId")
	private Integer empId;
	
	@Column(name = "FirstName")
	private String firstName;
	
	@Column(name = "LastName")
	private String lastName;
	
	@Column(name = "MobileNo")
	private Long mobileNo;
	
	@Column(name = "EmailId")
	private String emailId;
	
	@Column(name = "designation")
	private String designation;
	
	@Column(name = "JoiningDate")
	private String joinDate;
	
	@Column(name = "Gender")
	private String gender;
	
	@Column(name = "DOB")
	private String dob;
	
	@Column(name = "MaritalStatus")
	private String maritalStatus;

	public Integer getEmpId() {
		return empId;
	}

	public void setEmpId(Integer empId) {
		this.empId = empId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Long getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(Long mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(String joinDate) {
		this.joinDate = joinDate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	public Employee(Integer empId, String firstName, String lastName, Long mobileNo, String emailId, String designation,
			String joinDate, String gender, String dob, String maritalStatus) {
		super();
		this.empId = empId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.mobileNo = mobileNo;
		this.emailId = emailId;
		this.designation = designation;
		this.joinDate = joinDate;
		this.gender = gender;
		this.dob = dob;
		this.maritalStatus = maritalStatus;
	}

	public Employee() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "Employee [empId=" + empId + ", firstName=" + firstName + ", lastName=" + lastName + ", mobileNo="
				+ mobileNo + ", emailId=" + emailId + ", designation=" + designation + ", joinDate=" + joinDate
				+ ", gender=" + gender + ", dob=" + dob + ", maritalStatus=" + maritalStatus + "]";
	}
	
}
