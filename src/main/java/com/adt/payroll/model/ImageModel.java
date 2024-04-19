package com.adt.payroll.model;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
