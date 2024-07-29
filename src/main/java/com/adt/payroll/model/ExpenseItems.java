package com.adt.payroll.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(catalog = "EmployeeDB", schema = "expense_schema",name="ExpenseManagement")

public class ExpenseItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private int id;

    @Column(name="amount")
    private double amount;

    @Column(name="description")
    private String description;

    @Column(name="payment_mode")
    private String paymentMode;

    @Column(name="payment_date")
    private LocalDate paymentDate;

    @Column(name="created_by")
    private String createdBy;

    @Column(name="category")
    private String category;

    @Column(name="GST")
    private boolean gst;

    @Column(name="paid_by")
    private String paidBy;

    @Column(name="comments")
    private String comments;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "EMPLOYEE_ID" ,nullable = false, insertable = false, updatable = false)
    private User employee;
    private Integer employee_id;
    
    @Column(name="status")
    private String status;
}