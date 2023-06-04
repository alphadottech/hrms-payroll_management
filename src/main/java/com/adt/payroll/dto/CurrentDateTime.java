package com.adt.payroll.dto;

import lombok.Data;

@Data
public class CurrentDateTime {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private String currentDate;
    private String currentTime;
}
