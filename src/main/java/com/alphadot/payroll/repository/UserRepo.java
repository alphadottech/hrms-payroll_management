package com.alphadot.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alphadot.payroll.model.User;


public interface UserRepo extends JpaRepository<User, Integer>{

	
	

}