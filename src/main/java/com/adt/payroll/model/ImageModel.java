package com.adt.payroll.model;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
@Data
@Entity
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "image")
public class ImageModel {
	
	@Id
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "pic")
	private byte[] pic;

}
