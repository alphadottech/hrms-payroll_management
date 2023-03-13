package com.alphadot.payroll.errorResponse;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldErrors {
    private int status;
    private String message;
    private Map<String, String> field_errors;

    public FieldErrors(int status, String message) {
        this.status = status;
        this.message = message;
    }
}