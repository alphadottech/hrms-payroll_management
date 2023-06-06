package com.adt.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.adt.payroll.model.ImageModel;

public interface ImageRepo extends JpaRepository<ImageModel, Integer>{

	@Query(value = "SELECT pic FROM payroll_schema.image WHERE id = 1", nativeQuery=true)
	byte[] search();
}
