package com.adt.payroll.service;



import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.io.IOException;


import org.springframework.web.multipart.MultipartFile;

import com.adt.payroll.dto.CheckStatusDTO;
import com.adt.payroll.dto.EmployeeExpenseDTO;
import com.adt.payroll.dto.TimesheetDTO;
import com.adt.payroll.model.EmployeeExpense;
import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.payload.PriorTimeManagementRequest;
import com.adt.payroll.msg.ResponseModel;


public interface TimeSheetService {
	public String updateCheckIn(int empId,double latitude,double longitude);

	public String updateCheckOut(int empId,double latitude,double longitude) throws ParseException;

	CheckStatusDTO checkStatus(int empId);

	ResponseModel checkPriorStatus(int empId);

//-------------------------------------------------------------------------------------------------------------
	List<TimesheetDTO> empAttendence(int empId, String fromDate, String toDate);

	List<TimeSheetModel> allEmpAttendence(LocalDate fromDate, LocalDate toDate);

	Optional<Priortime> savePriorTime(PriorTimeManagementRequest priorTimeManagementRequest) throws ParseException;


	TimeSheetModel saveConfirmedDetails(Optional<Priortime> priortime) throws ParseException;

	public String pauseWorkingTime(int empId);

	public String resumeWorkingTime(int empId) throws ParseException;
	
	public EmployeeExpenseDTO employeeExpense(int empId, List<MultipartFile> image, EmployeeExpense employeeExpense) throws IOException;

	public EmployeeExpenseDTO acceptedEmployeeExpense(int expenseId, EmployeeExpenseDTO employeeExpenseDTO) throws IOException;

//	public EmployeeExpenseDTO rejectedEmployeeExpense(int expenseId);

	public EmployeeExpenseDTO getEmployeeExpenseById(int expenseId);
}
