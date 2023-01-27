package com.alphadot.payroll.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alphadot.payroll.service.PayRollService;

import java.text.ParseException;

@RestController
public class PayRollController {

    @Autowired
    private PayRollService fileService;
  
    @GetMapping("/readExcelFile")
    public String readExcelFile(@RequestParam("name") String name,@RequestParam("month") String month,@RequestParam("AddOn") int addOns,@RequestParam("MidPeriod") String midPeriod,@RequestParam("Add") int add) throws IOException, ParseException {
    	return this.fileService.readExcelFile(name,month,addOns,midPeriod,add);
    }
  
}
