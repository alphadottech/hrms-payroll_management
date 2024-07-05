package com.adt.payroll.model;


import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Proxy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(catalog = "EmployeeDB", schema = "user_schema", name = "_EMPLOYEE")
@Proxy(lazy = false)
@Data
public class User{

	@Id
	@Column(name = "EMPLOYEE_ID")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
	@SequenceGenerator(name = "user_seq", allocationSize = 1, schema = "user_schema")
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

	@Column(name = "Gender")
	private String gender;

	@Column(name = "ADT_ID",nullable = false,unique = true)
	private String adtId;


}
