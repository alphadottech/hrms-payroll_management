package com.adt.payroll.service;


import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.adt.payroll.dto.CheckStatusDTO;
import com.adt.payroll.dto.TimesheetDTO;
import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.payload.PriorTimeManagementRequest;
import com.adt.payroll.msg.ResponseModel;


public interface TimeSheetService {
	public String updateCheckIn(int empId);

	public String updateCheckOut(int id) throws ParseException;

	CheckStatusDTO checkStatus(int empId);

	ResponseModel checkPriorStatus(int empId);


	List<TimesheetDTO> empAttendence(int empId, LocalDate fromDate, LocalDate toDate);

	List<TimeSheetModel> allEmpAttendence(LocalDate fromDate, LocalDate toDate);

	Optional<Priortime> savePriorTime(PriorTimeManagementRequest priorTimeManagementRequest) throws ParseException;


	TimeSheetModel saveConfirmedDetails(Optional<Priortime> priortime) throws ParseException;

	public String pauseWorkingTime(int empId);

	public String resumeWorkingTime(int empId) throws ParseException;
}
