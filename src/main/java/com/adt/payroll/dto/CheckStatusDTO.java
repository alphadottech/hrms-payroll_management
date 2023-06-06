package com.adt.payroll.dto;

import lombok.Data;

@Data
public class CheckStatusDTO {
    private boolean checkIn;
    private boolean checkOut;
    private boolean pause;
    private boolean resume;
}
