package com.adt.payroll.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimesheetDTO {
    private int employeeId;
    private String date;
    private String status;
    private String workingHour;
    private String checkIn;
    private String checkOut;
    private String leaveInterval;
}
