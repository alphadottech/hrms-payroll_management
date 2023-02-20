package com.alphadot.payroll.service;

import java.text.ParseException;
import java.util.Optional;

import com.alphadot.payroll.model.Priortime;
import com.alphadot.payroll.model.TimeSheetModel;
import com.alphadot.payroll.model.payload.PriorTimeManagementRequest;

public interface TimeSheetService {

	String updateCheckIn(int id);

	String updateCheckOut(int id);

	Boolean saveStatus(int empId);

	Optional<Priortime> savePriorTime(PriorTimeManagementRequest priorTimeManagementRequest) throws ParseException;

	TimeSheetModel saveConfirmedDetails(Optional<Priortime> priortime) throws ParseException;
}
