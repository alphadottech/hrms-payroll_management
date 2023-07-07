package com.adt.payroll.dto;

import lombok.Data;


import java.io.File;
import java.util.HashMap;

@Data
public class EmployeeExpenseDTO {

    private int expenseId;

    private String employeeId;

    private String submitDate;

    private String expenseAmount;

    private String paymentDate;

    private String paymentMode;

    private String employeeComments;

    private String expenseCategory;

    private String expenseDescription;

    private String status;

    private HashMap<String, File> invoices;

    private String empName;

    private String empEmail;

    private String payrollComments;
}
