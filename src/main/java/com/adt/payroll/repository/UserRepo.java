package com.adt.payroll.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.adt.payroll.model.User;

public interface UserRepo extends JpaRepository<User, Integer> {

	@Query(value = "SELECT * FROM user_schema._employee where employee_id=?1", nativeQuery = true)
	Optional<User> findByEmployeeId(int empId);

}