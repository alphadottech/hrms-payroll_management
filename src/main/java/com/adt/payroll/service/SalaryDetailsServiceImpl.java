package com.adt.payroll.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adt.payroll.dto.SalaryDetailsDTO;
import com.adt.payroll.model.SalaryDetails;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.SalaryDetailsRepository;
import com.adt.payroll.repository.UserRepo;

@Service
public class SalaryDetailsServiceImpl implements SalaryDetailsService {

	private static final Logger log = LogManager.getLogger(SalaryDetailsServiceImpl.class);
	
	@Autowired
	public SalaryDetailsRepository salaryDetailsRepo;

	@Autowired
	public UserRepo userRepo;
	
	@Override
	public String saveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO) {
		log.info("PayrollService: SalaryDetailsController: Employee saveSalaryDetails: " + salaryDetailsDTO);

		String response = null;

		try {
			Optional<User> existEmployee = userRepo.findById(salaryDetailsDTO.getEmpId());
			if (existEmployee.isPresent()) {

				Optional<SalaryDetails> salaryDetails = salaryDetailsRepo.findByEmployeeId(salaryDetailsDTO.getEmpId());

				if (salaryDetails.isPresent()) {
					salaryDetails.get().setBasic(salaryDetailsDTO.getBasic());
					salaryDetails.get().setHouseRentAllowance(salaryDetailsDTO.getHouseRentAllowance());
					salaryDetails.get().setEmployeeESICAmount(salaryDetailsDTO.getEmployeeESICAmount());
					salaryDetails.get().setEmployerESICAmount(salaryDetailsDTO.getEmployerESICAmount());
					salaryDetails.get().setEmployeePFAmount(salaryDetailsDTO.getEmployeePFAmount());
					salaryDetails.get().setEmployerPFAmount(salaryDetailsDTO.getEmployerPFAmount());
					salaryDetails.get().setMedicalInsurance(salaryDetailsDTO.getMedicalInsurance());
					salaryDetails.get().setGrossSalary(salaryDetailsDTO.getGrossSalary());
					salaryDetails.get().setNetSalary(salaryDetailsDTO.getNetSalary());
					salaryDetailsRepo.save(salaryDetails.get());

				} else {
					SalaryDetails salaryDetailsNew = new SalaryDetails();

					salaryDetailsNew.setEmpId(salaryDetailsDTO.getEmpId());
					salaryDetailsNew.setBasic(salaryDetailsDTO.getBasic());
					salaryDetailsNew.setHouseRentAllowance(salaryDetailsDTO.getHouseRentAllowance());
					salaryDetailsNew.setEmployeeESICAmount(salaryDetailsDTO.getEmployeeESICAmount());
					salaryDetailsNew.setEmployerESICAmount(salaryDetailsDTO.getEmployerESICAmount());
					salaryDetailsNew.setEmployeePFAmount(salaryDetailsDTO.getEmployeePFAmount());
					salaryDetailsNew.setEmployerPFAmount(salaryDetailsDTO.getEmployerPFAmount());
					salaryDetailsNew.setMedicalInsurance(salaryDetailsDTO.getMedicalInsurance());
					salaryDetailsNew.setGrossSalary(salaryDetailsDTO.getGrossSalary());
					salaryDetailsNew.setNetSalary(salaryDetailsDTO.getNetSalary());
					salaryDetailsRepo.save(salaryDetailsNew);
				}
				response = "Success";

			} else {
				response = "NotExist";
			}
		} catch (Exception e) {

			e.printStackTrace();
			log.info("e.printStackTrace()---" + e.getMessage());
			response = "Failed";
		}

		return response;
	}

}
