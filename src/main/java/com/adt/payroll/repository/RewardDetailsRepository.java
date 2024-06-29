package com.adt.payroll.repository;

import com.adt.payroll.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardDetailsRepository extends JpaRepository<Reward,Integer> {

    //List<Reward> findByEmployee_EmpId(Integer empId);
    List<Reward> findByUser_Id(Integer employeeId);
}
