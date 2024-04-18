package com.adt.payroll.model;

import org.hibernate.annotations.Proxy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(catalog = "hrms_sit", schema = "payroll_schema", name = "emp_payroll_details")
@Proxy(lazy = false)
@Data
public class EmpPayrollDetails {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "salary")
    private Double salary;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "designation")
    private String designation;

    @Column(name = "JoiningDate")
    private String joinDate;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;


    @OneToOne
    @JoinColumn(name = "empId",referencedColumnName = "EMPLOYEE_ID", nullable = false, insertable = false, updatable = false)
    private User user;
    private int empId;
}