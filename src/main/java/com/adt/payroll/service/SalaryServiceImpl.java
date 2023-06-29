package com.adt.payroll.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adt.payroll.model.SalaryModel;
import com.adt.payroll.repository.SalaryRepo;

@Service
public class SalaryServiceImpl implements SalaryService {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SalaryRepo salaryRepo;

	@Override
	public List<SalaryModel> getAllEmpSalary() {
		List<SalaryModel> list=salaryRepo.findAll();
		LOGGER.info("Payroll service: SalaryServiceImpl:  getAllEmpSalary Info level log msg");

		return list;
	}

	public Optional<SalaryModel> getSalaryById(Integer empId) {
		LOGGER.info("Payroll service: SalaryServiceImpl:  getSalaryById Info level log msg");

		Optional<SalaryModel> model= salaryRepo.findByEmpId(empId);

		if(model==null) {
			throw new NullPointerException("No Data exist with given ID");
		}
		else {
			return model;
		}

	}

	@Override
	public String saveSalary(SalaryModel salaryModel) {
		salaryModel.setEmpName(salaryModel.getEmpName().toLowerCase());
		return "saved"+salaryRepo.save(salaryModel).getEmpId();
	}

	@Override
	public List<SalaryModel> searchByName(String name) {
		if(!name.isEmpty()) {
			List<SalaryModel> empList = salaryRepo.searchByEmpName(name.toLowerCase());
			return empList;
		}
	      else{
			  return null;
		}
	}
}
