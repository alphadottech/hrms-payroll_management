package com.adt.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryDTO {
    private Double adhoc;
    private Double employeePFAmount;
    private Double employerPFAmount;
    private Double employeeESICAmount;
    private Double employerESICAmount;
    private Double grossDeduction;
}