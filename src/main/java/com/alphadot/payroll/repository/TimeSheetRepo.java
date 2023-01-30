package com.alphadot.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alphadot.payroll.model.TimeSheetModel;

@Repository
public interface TimeSheetRepo extends JpaRepository<TimeSheetModel, Integer>{

    TimeSheetModel findByEmployeeIdAndDate(int id, String localDates);



    TimeSheetModel findByEmployeeId(int id);



    void deleteByEmployeeIdAndDate(int id,String localDates);



	List<TimeSheetModel> findAllByEmployeeId(int empId);



//	TimeSheetModel findByEmployeeIdAndDate(int empId, String date);


}
