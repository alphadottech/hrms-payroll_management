package com.adt.payroll.dto;

import lombok.Data;

@Data
public class AppraisalDetailsDTO {

    private Integer appr_hist_id;
    private int empId;
    private String name;
    private String year;
    private String month;
    private Double bonus;
    private Double variable;
    private Double amount;
    private String appraisalDate;
    private Double salary;
}