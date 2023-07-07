package com.adt.payroll.event;


import org.springframework.context.ApplicationEvent;

import com.adt.payroll.dto.EmployeeExpenseDTO;

public class OnEmployeeExpenseAcceptOrRejectEvent extends ApplicationEvent {
    private EmployeeExpenseDTO employeeExpenseDTO;
    private String action;
    private String actionStatus;

    public OnEmployeeExpenseAcceptOrRejectEvent(EmployeeExpenseDTO employeeExpenseDTO, String action, String actionStatus) {
        super(employeeExpenseDTO);
        this.employeeExpenseDTO = employeeExpenseDTO;
        this.action = action;
        this.actionStatus = actionStatus;
    }

    public String getAction() {
        return action;
    }

    public EmployeeExpenseDTO getEmployeeExpenseDTO() {
        return employeeExpenseDTO;
    }

    public void setEmployeeExpenseDTO(EmployeeExpenseDTO employeeExpenseDTO) {
        this.employeeExpenseDTO = employeeExpenseDTO;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
    }
}