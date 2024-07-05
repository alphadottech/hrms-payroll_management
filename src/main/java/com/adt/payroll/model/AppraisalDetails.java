package com.adt.payroll.model;

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
@Table(catalog = "hrms_sit", schema = "payroll_schema", name = "appraisal_historical_details")
public class AppraisalDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "appraisal_historical_details_seq")
    @SequenceGenerator(name = "appraisal_historical_details_seq", allocationSize = 1, schema = "payroll_schema")
    @Column(name = "appr_hist_id")
    private Integer appr_hist_id;

    @Column(name = "year")
    private String year;

    @Column(name = "month")
    private String month;

    @Column(name = "bonus")
    private Double bonus;

    @Column(name = "variable")
    private Double variable;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "initiated_date")
    private String initiatedDate;

    @Column(name = "appraisal_date")
    private String appraisalDate;

    @Column(name = "salary")
    private Double salary;

    @ManyToOne
    @JoinColumn(name = "empId", referencedColumnName = "EMPLOYEE_ID" ,nullable = false, insertable = false, updatable = false)
    private User employee;
    private Integer empId;

}