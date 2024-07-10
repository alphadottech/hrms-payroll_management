package com.adt.payroll.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adt.payroll.model.User;

public interface UserRepo extends JpaRepository<User, Integer> {

	@Query(value = "SELECT * FROM user_schema._employee where employee_id=?1", nativeQuery = true)
	Optional<User> findByEmployeeId(int empId);

	@Query(value = "SELECT * FROM user_schema._employee e "
			+ "WHERE LOWER(e.first_name) LIKE CONCAT(LOWER(:firstName), '%') "
			+ "AND (e.last_name IS NOT NULL AND LOWER(e.last_name) LIKE CONCAT(LOWER(:lastName), '%'))", nativeQuery = true)
	List<User> findByFirstNameAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);

}