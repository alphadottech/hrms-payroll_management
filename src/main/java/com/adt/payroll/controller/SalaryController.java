package com.adt.payroll.controller;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.adt.payroll.model.SalaryModel;
import com.adt.payroll.service.SalaryService;

@RestController
@RequestMapping("/salary")
public class SalaryController {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SalaryService salaryService;

    @PreAuthorize("@auth.allow('ROLE_ADMIN')")
    @GetMapping("/getAllEmpSalary")
    public ResponseEntity<List<SalaryModel>> getAllEmpSalary() throws ParseException {
        LOGGER.info("Payroll service: salary:  getAllEmpSalary() Info level log msg");
        return new ResponseEntity<>(salaryService.getAllEmpSalary(), HttpStatus.OK);
    }

    @PreAuthorize("@auth.allow('ROLE_USER',T(java.util.Map).of('currentUser', #empId))")
    @GetMapping("/getSalaryById/{empId}")
    public ResponseEntity<Optional<SalaryModel>> getSalaryById(@PathVariable("empId") Integer empId) {
		LOGGER.info("Payroll service: salary:  getSalaryById Info level log msg");
		return new ResponseEntity<>(salaryService.getSalaryById(empId), HttpStatus.OK);
    }

    @PreAuthorize("@auth.allow('ROLE_ADMIN')")
    @PostMapping("/saveSalary")
    public ResponseEntity<String> saveSalary(@RequestBody SalaryModel salaryModel){
        LOGGER.info("Payroll service: salary:  create salary for employee Info level log msg");
        return new ResponseEntity<>(salaryService.saveSalary(salaryModel), HttpStatus.OK);
    }
    @PreAuthorize("@auth.allow('ROLE_ADMIN')")
    @GetMapping("/searchByName")
    public ResponseEntity<List<SalaryModel>> searchByName(@RequestParam("name") String name){
        LOGGER.info("Payroll service: salary:  search employee By Name Info level log msg");
        return new ResponseEntity<>(salaryService.searchByName(name), HttpStatus.OK);
    }

}
