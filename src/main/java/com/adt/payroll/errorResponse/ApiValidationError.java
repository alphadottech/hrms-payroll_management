package com.adt.payroll.errorResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

abstract class ApiSubError {

}

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiValidationError extends ApiSubError {
    private String field;
    private String message;

    public ApiValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }
}