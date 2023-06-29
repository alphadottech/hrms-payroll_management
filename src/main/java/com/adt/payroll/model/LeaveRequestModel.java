package com.adt.payroll.model;


import java.util.List;

import javax.persistence.*;

import lombok.Data;

@Data
@Entity
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "LeaveRequest")
public class LeaveRequestModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leaveid", columnDefinition = "serial")
    private Integer leaveid;

    @Column(name = "empid")
    private Integer empid;

    @Column(name = "status")
    private String status;

	@Transient
    private Integer leaveBalance;
	@Transient
    private String leaveType;
	@Transient
    private String leaveReason;
	@Transient
    private String name;

    @ElementCollection
    @CollectionTable(catalog = "EmployeeDB", schema = "payroll_schema", name = "LEAVE_DATES", joinColumns = @JoinColumn(name = "LEAVE_ID"))
    @Column(name = "leavedate")
    private List<String> leavedate;


}
