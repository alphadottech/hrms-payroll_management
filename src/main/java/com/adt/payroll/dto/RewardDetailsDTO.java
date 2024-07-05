package com.adt.payroll.dto;

import lombok.Data;

@Data
public class RewardDetailsDTO {
    private Integer id;
    private String name;
    private String rewardType;
    private double amount;
    private String effectiveDate;


}
