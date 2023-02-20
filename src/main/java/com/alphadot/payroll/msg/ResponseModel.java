package com.alphadot.payroll.msg;

import java.util.List;

import lombok.Data;

@Data
public class ResponseModel {

	private String msg;
	private Boolean timeSheetStatus;
	private List<String> priorResult;
}

