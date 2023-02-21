package com.alphadot.payroll.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Proxy;

import lombok.Data;

@Entity
@Table(catalog = "EmployeeDB", schema = "user_schema", name = "_EMPLOYEE")
@Proxy(lazy = false)
@Data
public class User{

	@Id
	@Column(name = "EMPLOYEE_ID")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
	@SequenceGenerator(name = "user_seq", allocationSize = 1)
	private int id;

	@NaturalId
	@Column(name = "EMAIL", unique = true)
	
	private String email;

	@Column(name = "USERNAME", unique = true)

	private String username;

	@Column(name = "PASSWORD")

	private String password;

	@Column(name = "FIRST_NAME")

	private String firstName;

	@Column(name = "LAST_NAME")
	
	private String lastName;
	
	@Column(name = "IS_ACTIVE", nullable = false)
	private Boolean isActive;

	@Column(name = "IS_EMAIL_VERIFIED", nullable = false)
	private Boolean isEmailVerified;

	@Column(name = "MobileNo")
	private Long mobileNo;

	@Column(name = "designation")
	private String designation;

	@Column(name = "JoiningDate")
	private String joinDate;
	
	@Column(name = "salary")
	private double salary;

	@Column(name = "Gender")
	private String gender;

//	@Column(name = "DOB")
//	private String dob;

	@Column(name = "bank_name")
	private String bankName;
	
	@Column(name = "account_number")
	private String accountNumber;
//	
//	@Column(name = "ifsc_code")
//	private String ifscCode;
//	
//	@Column(name = "created_at")
//	private LocalTime createdAt;
//	
//	@Column(name = "updated_at")
//	private LocalTime updatedAt;
}
