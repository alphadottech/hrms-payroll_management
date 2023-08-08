package com.adt.payroll.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.util.UriComponentsBuilder;

import com.adt.payroll.dto.EmployeeExpenseDTO;

public class OnEmployeeExpenseDetailsSavedEvent extends ApplicationEvent {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private transient UriComponentsBuilder redirectUrl1;

    private transient UriComponentsBuilder redirectUrl2;

    private EmployeeExpenseDTO employeeExpenseDTO;

    public OnEmployeeExpenseDetailsSavedEvent(EmployeeExpenseDTO employeeExpenseDTO, UriComponentsBuilder redirectUrl1,UriComponentsBuilder redirectUrl2) {
        super(employeeExpenseDTO);
        this.employeeExpenseDTO = employeeExpenseDTO;
        this.redirectUrl1 = redirectUrl1;
        this.redirectUrl2 = redirectUrl2;
    }


//	public UriComponentsBuilder getRedirectUrl() {
//		return redirectUrl;
//	}
//
//	public void setRedirectUrl(UriComponentsBuilder redirectUrl) {
//		this.redirectUrl = redirectUrl;
//	}

    public UriComponentsBuilder getRedirectUrl1() {
        return redirectUrl1;
    }

    public UriComponentsBuilder getRedirectUrl2() {
        return redirectUrl2;
    }
    public void setRedirectUrl1(UriComponentsBuilder redirectUrl1) {
        this.redirectUrl1 = redirectUrl1;
    }

    public void setRedirectUrl2(UriComponentsBuilder redirectUrl2) {
        this.redirectUrl2 = redirectUrl2;
    }
    public EmployeeExpenseDTO getEmployeeExpenseDTO() {
        return employeeExpenseDTO;
    }

    public void setEmployeeExpenseDTO(EmployeeExpenseDTO employeeExpenseDTO) {
        this.employeeExpenseDTO = employeeExpenseDTO;
    }
}
