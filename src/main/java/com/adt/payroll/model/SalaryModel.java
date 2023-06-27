package com.adt.payroll.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "salary_table")
public class SalaryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "serial_id")
    private int serialNo;

    @Column(name = "employee")
    private String empName;

    @Column(name = "emp_id")
    private Integer empId;

    @Column(name = "email")
    private String email;

    @Column(name = "join_date")
    private String joinDate;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "role")
    private String role;

    @Column(name = "salary")
    private float salary;
}