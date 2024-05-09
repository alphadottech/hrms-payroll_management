package com.adt.payroll.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "priortime_table")
@Data
public class Priortime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "priortime_id", columnDefinition = "serial")
	private int priortimeId;

	@Column(name = "employee_id")
	private int employeeId;

	@Column(name = "checkIn")
	private String checkIn;

	@Column(name = "checkOut")
	private String checkOut;

	@Column(name = "date")
	private String date;

	@Column(name = "month")
	private String month;

	@Column(name = "status")
	private String status;

	@Column(name = "workingHour")
	private String workingHour;

	@Column(name = "year")
	private String year;

	@Column(name = "email")
	private String email;
	
	@Column(name = "checkInLatitude")
    private String checkInLatitude;

    @Column(name = "checkInLongitude")
    private String checkInLongitude;

    @Column(name = "checkInDistance")
    private String checkInDistance;

    @Column(name = "checkOutLatitude")
    private String checkOutLatitude;

    @Column(name = "checkOutLongitude")
    private String checkOutLongitude;

    @Column(name = "checkOutDistance")
    private String checkOutDistance;
	

}
