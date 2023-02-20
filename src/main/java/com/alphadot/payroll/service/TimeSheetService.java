package com.alphadot.payroll.service;

import java.time.LocalDate;
import java.util.List;

import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.msg.ResponseModel;

public interface TimeSheetService {
	ResponseModel updateCheckIn(int id);

	ResponseModel updateCheckOut(int id);

	ResponseModel checkStatus(int empId);

	ResponseModel checkPriorStatus(int empId);

	List<TimeSheetModel> empAttendence(int empId, LocalDate fromDate, LocalDate toDate);

	List<TimeSheetModel> allEmpAttendence(LocalDate fromDate, LocalDate toDate);

}
