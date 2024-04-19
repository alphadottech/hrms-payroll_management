package com.adt.payroll.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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