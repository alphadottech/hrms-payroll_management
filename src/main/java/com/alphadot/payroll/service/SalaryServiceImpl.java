package com.alphadot.payroll.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alphadot.payroll.model.SalaryModel;
import com.alphadot.payroll.repository.SalaryRepo;

@Service
public class SalaryServiceImpl implements SalaryService {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SalaryRepo salaryRepo;

	@Override
	public List<SalaryModel> getAllEmpSalary() {
		List<SalaryModel> list = salaryRepo.findAll();
		LOGGER.info("Payroll service: SalaryServiceImpl:  getAllEmpSalary Info level log msg");
		return list;
	}

	public Optional<SalaryModel> getSalaryById(int empId) {
		LOGGER.info("Payroll service: SalaryServiceImpl:  getSalaryById Info level log msg");
		Optional<SalaryModel> model = salaryRepo.findById(empId);
		if (model == null) {
			throw new NullPointerException("No Data exist with given ID");
		} else {
			return model;
		}
	}
}
