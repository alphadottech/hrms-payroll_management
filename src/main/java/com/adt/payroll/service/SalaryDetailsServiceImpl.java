package com.adt.payroll.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.adt.payroll.dto.SalaryDetailsDTO;
import com.adt.payroll.model.EmpPayrollDetails;
import com.adt.payroll.model.SalaryDetails;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.EmpPayrollDetailsRepo;
import com.adt.payroll.repository.SalaryDetailsRepository;
import com.adt.payroll.repository.UserRepo;

@Service
public class SalaryDetailsServiceImpl implements SalaryDetailsService {

	private static final Logger log = LogManager.getLogger(SalaryDetailsServiceImpl.class);

	@Autowired
	public EmpPayrollDetailsRepo empPayrollDetailsRepo;

	@Autowired
	public SalaryDetailsRepository salaryDetailsRepo;

	@Autowired
	public UserRepo userRepo;

	@Override
	public ResponseEntity<String> saveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO) {
		log.info("PayrollService: SalaryDetailsController: Employee saveSalaryDetails: " + salaryDetailsDTO);

		try {
			Optional<User> existEmployee = userRepo.findById(salaryDetailsDTO.getEmpId());
			if (existEmployee.isPresent()) {

				Optional<EmpPayrollDetails> empPayrollOptional = empPayrollDetailsRepo
						.findByEmployeeId(salaryDetailsDTO.getEmpId());
				Optional<SalaryDetails> salaryDetails = salaryDetailsRepo.findByEmployeeId(salaryDetailsDTO.getEmpId());

				if (empPayrollOptional.isPresent()) {

					empPayrollOptional.get().setSalary(salaryDetailsDTO.getSalary());
					empPayrollOptional.get().setBankName(salaryDetailsDTO.getBankName());
					empPayrollOptional.get().setDesignation(salaryDetailsDTO.getDesignation());
					empPayrollOptional.get().setJoinDate(salaryDetailsDTO.getJoinDate());
					empPayrollOptional.get().setAccountNumber(salaryDetailsDTO.getAccountNumber());
					empPayrollOptional.get().setIfscCode(salaryDetailsDTO.getIfscCode());
					empPayrollDetailsRepo.save(empPayrollOptional.get());

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

						return new ResponseEntity<>("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
								+ " is Updated Succesfully", HttpStatus.OK);

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

						return new ResponseEntity<>("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
								+ " is Saved Succesfully", HttpStatus.OK);
					}
				} else {

					EmpPayrollDetails empPayrollDetailsNew = new EmpPayrollDetails();

					empPayrollDetailsNew.setEmpId(salaryDetailsDTO.getEmpId());
					empPayrollDetailsNew.setSalary(salaryDetailsDTO.getSalary());
					empPayrollDetailsNew.setBankName(salaryDetailsDTO.getBankName());
					empPayrollDetailsNew.setDesignation(salaryDetailsDTO.getDesignation());
					empPayrollDetailsNew.setJoinDate(salaryDetailsDTO.getJoinDate());
					empPayrollDetailsNew.setAccountNumber(salaryDetailsDTO.getAccountNumber());
					empPayrollDetailsNew.setIfscCode(salaryDetailsDTO.getIfscCode());
					empPayrollDetailsRepo.save(empPayrollDetailsNew);

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

					return new ResponseEntity<>(
							"EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId() + " is Saved Succesfully",
							HttpStatus.OK);
				}
			} else {
				return new ResponseEntity<>(
						"EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId() + " is Not Exist",
						HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {

			e.printStackTrace();
			log.info("e.printStackTrace()---" + e.getMessage());
			return new ResponseEntity<>(
					"EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId() + " is Not Saved",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
