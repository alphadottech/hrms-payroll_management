package com.alphadot.payroll.service;


public interface TimeSheetService {

	String updateCheckIn(int id);

	String updateCheckOut(int id);

	Boolean saveStatus(int empId);


}
