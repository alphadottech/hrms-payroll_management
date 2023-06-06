package com.adt.payroll.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adt.payroll.model.User;


public interface UserRepo extends JpaRepository<User, Integer>{

	

	
	

}