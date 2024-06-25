package com.adt.payroll.service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;


import com.adt.payroll.model.*;
import com.adt.payroll.repository.AppraisalDetailsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.adt.payroll.dto.SalaryDetailsDTO;
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

	@Autowired
    private AppraisalDetailsRepository appraisalDetailsRepository;

	@Autowired
	private CommonEmailService mailService;

	@Override
	public ResponseEntity<String> saveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO) {
		log.info("PayrollService: SalaryDetailsController: Employee saveSalaryDetails: " + salaryDetailsDTO);
		boolean isEsic = false;
		try {
			Optional<User> existEmployee = userRepo.findById(salaryDetailsDTO.getEmpId());
			String name = existEmployee.get().getFirstName() + " " + existEmployee.get().getLastName();
			if (existEmployee.isPresent()) {

				Optional<EmpPayrollDetails> empPayrollExist = empPayrollDetailsRepo
						.findByEmployeeId(salaryDetailsDTO.getEmpId());
				Optional<SalaryDetails> salaryDetailsExist = salaryDetailsRepo.findByEmployeeId(salaryDetailsDTO.getEmpId());

				if (empPayrollExist.isPresent()) {
					if (salaryDetailsDTO.getSalary() <= 21000) {
						isEsic = true;
					}
					EmpPayrollDetails updateEmpPayroll= empPayrollExist.get();
					updateEmpPayroll.setSalary(salaryDetailsDTO.getSalary());
					updateEmpPayroll.setBankName(salaryDetailsDTO.getBankName());
					updateEmpPayroll.setDesignation(salaryDetailsDTO.getDesignation());
					updateEmpPayroll.setJoinDate(salaryDetailsDTO.getJoinDate());
					updateEmpPayroll.setAccountNumber(salaryDetailsDTO.getAccountNumber());
					updateEmpPayroll.setIfscCode(salaryDetailsDTO.getIfscCode());
					empPayrollDetailsRepo.save(updateEmpPayroll);

					if (salaryDetailsExist.isPresent()) {
						SalaryDetails updateEmpsalary=salaryDetailsExist.get();
						if (validatePFAndEsicAmount(salaryDetailsDTO, isEsic, name)) {
							updateEmpsalary.setBasic(salaryDetailsDTO.getBasic());
							updateEmpsalary.setHouseRentAllowance(salaryDetailsDTO.getHouseRentAllowance());
							updateEmpsalary.setEmployeeESICAmount(salaryDetailsDTO.getEmployeeESICAmount());
							updateEmpsalary.setEmployerESICAmount(salaryDetailsDTO.getEmployerESICAmount());
							updateEmpsalary.setEmployeePFAmount(salaryDetailsDTO.getEmployeePFAmount());
							updateEmpsalary.setEmployerPFAmount(salaryDetailsDTO.getEmployerPFAmount());
							updateEmpsalary.setMedicalInsurance(salaryDetailsDTO.getMedicalInsurance());
							updateEmpsalary.setGrossSalary(salaryDetailsDTO.getGrossSalary());
							updateEmpsalary.setNetSalary(salaryDetailsDTO.getNetSalary());
							salaryDetailsRepo.save(updateEmpsalary);

							return new ResponseEntity<>("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
									+ " is Updated Succesfully", HttpStatus.OK);
						} else {
							return new ResponseEntity<>("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
									+ " inserted data is incorrect. Please check the mail.", HttpStatus.OK);
						}

					} else {

						SalaryDetails saveEmpsalary = new SalaryDetails();
						if (validatePFAndEsicAmount(salaryDetailsDTO, isEsic, name)) {
							saveEmpsalary.setEmpId(salaryDetailsDTO.getEmpId());
							saveEmpsalary.setBasic(salaryDetailsDTO.getBasic());
							saveEmpsalary.setHouseRentAllowance(salaryDetailsDTO.getHouseRentAllowance());
							saveEmpsalary.setEmployeeESICAmount(salaryDetailsDTO.getEmployeeESICAmount());
							saveEmpsalary.setEmployerESICAmount(salaryDetailsDTO.getEmployerESICAmount());
							saveEmpsalary.setEmployeePFAmount(salaryDetailsDTO.getEmployeePFAmount());
							saveEmpsalary.setEmployerPFAmount(salaryDetailsDTO.getEmployerPFAmount());
							saveEmpsalary.setMedicalInsurance(salaryDetailsDTO.getMedicalInsurance());
							saveEmpsalary.setGrossSalary(salaryDetailsDTO.getGrossSalary());
							saveEmpsalary.setNetSalary(salaryDetailsDTO.getNetSalary());
							salaryDetailsRepo.save(saveEmpsalary);

							return new ResponseEntity<>("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
									+ " is Saved Succesfully", HttpStatus.OK);
						} else {
							return new ResponseEntity<>("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
									+ " inserted data is incorrect. Please check the mail.", HttpStatus.OK);
						}
					}
				} else {

					EmpPayrollDetails saveEmpPayroll = new EmpPayrollDetails();

					saveEmpPayroll.setEmpId(salaryDetailsDTO.getEmpId());
					saveEmpPayroll.setSalary(salaryDetailsDTO.getSalary());
					saveEmpPayroll.setBankName(salaryDetailsDTO.getBankName());
					saveEmpPayroll.setDesignation(salaryDetailsDTO.getDesignation());
					saveEmpPayroll.setJoinDate(salaryDetailsDTO.getJoinDate());
					saveEmpPayroll.setAccountNumber(salaryDetailsDTO.getAccountNumber());
					saveEmpPayroll.setIfscCode(salaryDetailsDTO.getIfscCode());
					empPayrollDetailsRepo.save(saveEmpPayroll);

					SalaryDetails saveEmpSalary = new SalaryDetails();
					if (validatePFAndEsicAmount(salaryDetailsDTO, isEsic, name)) {
						saveEmpSalary.setEmpId(salaryDetailsDTO.getEmpId());
						saveEmpSalary.setBasic(salaryDetailsDTO.getBasic());
						saveEmpSalary.setHouseRentAllowance(salaryDetailsDTO.getHouseRentAllowance());
						saveEmpSalary.setEmployeeESICAmount(salaryDetailsDTO.getEmployeeESICAmount());
						saveEmpSalary.setEmployerESICAmount(salaryDetailsDTO.getEmployerESICAmount());
						saveEmpSalary.setEmployeePFAmount(salaryDetailsDTO.getEmployeePFAmount());
						saveEmpSalary.setEmployerPFAmount(salaryDetailsDTO.getEmployerPFAmount());
						saveEmpSalary.setMedicalInsurance(salaryDetailsDTO.getMedicalInsurance());
						saveEmpSalary.setGrossSalary(salaryDetailsDTO.getGrossSalary());
						saveEmpSalary.setNetSalary(salaryDetailsDTO.getNetSalary());
						salaryDetailsRepo.save(saveEmpSalary);

						return new ResponseEntity<>("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
								+ " is Saved Succesfully", HttpStatus.OK);
					} else {
						return new ResponseEntity<>("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
								+ " inserted data is incorrect. Please check the mail.", HttpStatus.OK);
					}
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

	private boolean validatePFAndEsicAmount(SalaryDetailsDTO dto, boolean isESIC, String name) {
		double salary = dto.getSalary();
		double actualBasic = salary / 2;
		double grossSalaryAmount = salary;
		double basic=0.0;
		// employer pf and esic portion calculation 13% and 0.75% respectively
		if (dto.getBasic() <= 15000) {
			actualBasic=dto.getBasic();
		}
		double employerPFAmount = actualBasic * 0.13;
		double employerESICAmount = grossSalaryAmount * 0.0075;

		// employer pf and esic portion calculation 12% and 3.25% respectively
		double employeeESICAmount = grossSalaryAmount * 0.0325;
		List<String> errorMsg = new ArrayList();
		String msg = "";
		if (isESIC) {
			if (Math.abs(employerESICAmount - dto.getEmployerESICAmount()) > 100) {
				msg = "Employer Esic amount different exceeds the difference limit of eSICAmount & fetched employerESICAmount "
						+ Math.abs(employerESICAmount - dto.getEmployerESICAmount());
				errorMsg.add(msg);
			}

			if (Math.abs(employeeESICAmount - dto.getEmployeeESICAmount()) > 100) {
				msg = "Employee Esic amount different exceeds the difference limit."
						+ Math.abs(employeeESICAmount - dto.getEmployeeESICAmount());
				errorMsg.add(msg);
			}
		}
		
		if (Math.abs(employerPFAmount - dto.getEmployerPFAmount()) > 100) {
			msg = "Employer pf amount difference exceeds the difference limit."
					+ Math.abs(employerPFAmount - dto.getEmployerPFAmount());
			errorMsg.add(msg);
		}
		if (isESIC) {
//				grossSalaryAmount = Math.round(grossSalaryAmount - employerPFAmount
//						- (employeeESICAmount + employerESICAmount) + (grossSalaryAmount * 0.01617));
				grossSalaryAmount = Math.round(
						grossSalaryAmount - employerPFAmount - employerESICAmount + (grossSalaryAmount * 0.01617));

			} else {

				grossSalaryAmount = Math.round(grossSalaryAmount - employerPFAmount + (grossSalaryAmount * 0.01617));
			}
		
		if (Math.abs(grossSalaryAmount - dto.getGrossSalary()) > 100) {
			msg = "gross amount different exceeds the difference limit."
					+ Math.abs(grossSalaryAmount - dto.getGrossSalary());
			errorMsg.add(msg);
		}
		if(dto.getBasic()<=15000) {
			basic =dto.getBasic();
		}else {
			basic = grossSalaryAmount / 2;
		}
		
		double empCalcutedPFAmount = basic * 0.12;
		if (Math.abs(empCalcutedPFAmount - dto.getEmployeePFAmount()) > 100) {
			msg = "Employee pf amount different exceeds the difference limit."
					+ Math.abs(empCalcutedPFAmount - dto.getEmployeePFAmount());
			errorMsg.add(msg);
		}

		if (!errorMsg.isEmpty() && errorMsg.size() > 0) {
			mailService.sendEmail(name, errorMsg.toString());
			return false;
		}
		return true;
	}
	
	private ResponseEntity<SalaryDetailsDTO> calculatePFAndEsicAmount(SalaryDetailsDTO dto, boolean isEsic, String name) {
		log.info("calculatePFAndEsicAmount called");
		try {
			SalaryDetailsDTO response = dto;
			double salary = dto.getSalary();
			double actualBasic = salary / 2;
			double grossSalaryAmount = salary;
			double basic = 0.0;
			// employer pf and esic portion calculation 13% and 0.75% respectively
			if (dto.getBasic() <= 15000 || actualBasic == dto.getBasic()) {
				actualBasic = dto.getBasic();
				response.setHouseRentAllowance(salary - actualBasic);
			}
			double employerPFAmount = actualBasic * 0.13;
			
			double employerESICAmount = grossSalaryAmount * 0.0075;
			// employer pf and esic portion calculation 12% and 3.25% respectively
			double employeeESICAmount = grossSalaryAmount * 0.0325;
			response.setEmployerPFAmount(employerPFAmount);
			if (isEsic) {
				response.setEmployeeESICAmount(employeeESICAmount);
				response.setEmployerESICAmount(employerESICAmount);
//				grossSalaryAmount = Math.round(grossSalaryAmount - employerPFAmount
//						- (employeeESICAmount + employerESICAmount) + (grossSalaryAmount * 0.01617));
				grossSalaryAmount = Math.round(
						grossSalaryAmount - employerPFAmount - employerESICAmount + (grossSalaryAmount * 0.01617));

			} else {

				grossSalaryAmount = Math.round(grossSalaryAmount - employerPFAmount + (grossSalaryAmount * 0.01617));
			}
			response.setGrossSalary(grossSalaryAmount);

			if (dto.getBasic() == 15000) {
				basic = dto.getBasic();
			} else {
				basic = grossSalaryAmount / 2;
			}

			double empCalcutedPFAmount = basic * 0.12;
			response.setEmployeePFAmount(empCalcutedPFAmount);
			//gross-> basic and hra saved
			response.setBasic(basic);
			response.setHouseRentAllowance(grossSalaryAmount - basic);

			response.setOnlyBasic(false);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("e.printStackTrace()---" + e.getMessage());
			return new ResponseEntity("Calculating salary amount of EmpId:" + dto.getEmpId() + " is Not Saved",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Override
	public ResponseEntity<SalaryDetailsDTO> calculateAndSaveSalaryDetails(SalaryDetailsDTO salaryDetailsDTO) {
		log.info("PayrollService: SalaryDetailsController: Employee saveSalaryDetails: " + salaryDetailsDTO);
		boolean isEsic = false;
		try {
			Optional<User> existEmployee = userRepo.findById(salaryDetailsDTO.getEmpId());
			String name = existEmployee.get().getFirstName() + " " + existEmployee.get().getLastName();
			if (existEmployee.isPresent()) {
				if (salaryDetailsDTO.getSalary() <= 21000) {
					isEsic = true;
				}
				// here we calculate the pf and esic on the basis of given basic
				if(salaryDetailsDTO.isOnlyBasic()) {
					return calculatePFAndEsicAmount(salaryDetailsDTO, isEsic, name);
				}
				
				Optional<EmpPayrollDetails> empPayrollExist = empPayrollDetailsRepo
						.findByEmployeeId(salaryDetailsDTO.getEmpId());
				Optional<SalaryDetails> salaryDetailsExist = salaryDetailsRepo.findByEmployeeId(salaryDetailsDTO.getEmpId());
				
				if (empPayrollExist.isPresent()) {
					
					EmpPayrollDetails updateEmpPayroll= empPayrollExist.get();
					updateEmpPayroll.setSalary(salaryDetailsDTO.getSalary());
					updateEmpPayroll.setBankName(salaryDetailsDTO.getBankName());
					updateEmpPayroll.setDesignation(salaryDetailsDTO.getDesignation());
					updateEmpPayroll.setJoinDate(salaryDetailsDTO.getJoinDate());
					updateEmpPayroll.setAccountNumber(salaryDetailsDTO.getAccountNumber());
					updateEmpPayroll.setIfscCode(salaryDetailsDTO.getIfscCode());
					updateEmpPayroll.setVariable(salaryDetailsDTO.getVariableAmount());

					empPayrollDetailsRepo.save(updateEmpPayroll);

					if (salaryDetailsExist.isPresent()) {
						SalaryDetails updateEmpsalary=salaryDetailsExist.get();
						
							updateEmpsalary.setBasic(salaryDetailsDTO.getBasic());
							updateEmpsalary.setHouseRentAllowance(salaryDetailsDTO.getHouseRentAllowance());
							updateEmpsalary.setEmployeeESICAmount(salaryDetailsDTO.getEmployeeESICAmount());
							updateEmpsalary.setEmployerESICAmount(salaryDetailsDTO.getEmployerESICAmount());
							updateEmpsalary.setEmployeePFAmount(salaryDetailsDTO.getEmployeePFAmount());
							updateEmpsalary.setEmployerPFAmount(salaryDetailsDTO.getEmployerPFAmount());
							updateEmpsalary.setMedicalInsurance(salaryDetailsDTO.getMedicalInsurance());
							updateEmpsalary.setGrossSalary(salaryDetailsDTO.getGrossSalary());
							updateEmpsalary.setNetSalary(salaryDetailsDTO.getNetSalary());
							salaryDetailsRepo.save(updateEmpsalary);

							return new ResponseEntity("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
									+ " is Updated Succesfully", HttpStatus.OK);

					} else {

						SalaryDetails saveEmpsalary = new SalaryDetails();
					
							saveEmpsalary.setEmpId(salaryDetailsDTO.getEmpId());
							saveEmpsalary.setBasic(salaryDetailsDTO.getBasic());
							saveEmpsalary.setHouseRentAllowance(salaryDetailsDTO.getHouseRentAllowance());
							saveEmpsalary.setEmployeeESICAmount(salaryDetailsDTO.getEmployeeESICAmount());
							saveEmpsalary.setEmployerESICAmount(salaryDetailsDTO.getEmployerESICAmount());
							saveEmpsalary.setEmployeePFAmount(salaryDetailsDTO.getEmployeePFAmount());
							saveEmpsalary.setEmployerPFAmount(salaryDetailsDTO.getEmployerPFAmount());
							saveEmpsalary.setMedicalInsurance(salaryDetailsDTO.getMedicalInsurance());
							saveEmpsalary.setGrossSalary(salaryDetailsDTO.getGrossSalary());
							saveEmpsalary.setNetSalary(salaryDetailsDTO.getVariableAmount());
							salaryDetailsRepo.save(saveEmpsalary);

							return new ResponseEntity("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
									+ " is Saved Succesfully", HttpStatus.OK);
					}
				} else {

					EmpPayrollDetails saveEmpPayroll = new EmpPayrollDetails();

					saveEmpPayroll.setEmpId(salaryDetailsDTO.getEmpId());
					saveEmpPayroll.setSalary(salaryDetailsDTO.getSalary());
					saveEmpPayroll.setBankName(salaryDetailsDTO.getBankName());
					saveEmpPayroll.setDesignation(salaryDetailsDTO.getDesignation());
					saveEmpPayroll.setJoinDate(salaryDetailsDTO.getJoinDate());
					saveEmpPayroll.setAccountNumber(salaryDetailsDTO.getAccountNumber());
					saveEmpPayroll.setIfscCode(salaryDetailsDTO.getIfscCode());
					saveEmpPayroll.setVariable(salaryDetailsDTO.getVariableAmount());
					empPayrollDetailsRepo.save(saveEmpPayroll);

					SalaryDetails saveEmpSalary = new SalaryDetails();
				
						saveEmpSalary.setEmpId(salaryDetailsDTO.getEmpId());
						saveEmpSalary.setBasic(salaryDetailsDTO.getBasic());
						saveEmpSalary.setHouseRentAllowance(salaryDetailsDTO.getHouseRentAllowance());
						saveEmpSalary.setEmployeeESICAmount(salaryDetailsDTO.getEmployeeESICAmount());
						saveEmpSalary.setEmployerESICAmount(salaryDetailsDTO.getEmployerESICAmount());
						saveEmpSalary.setEmployeePFAmount(salaryDetailsDTO.getEmployeePFAmount());
						saveEmpSalary.setEmployerPFAmount(salaryDetailsDTO.getEmployerPFAmount());
						saveEmpSalary.setMedicalInsurance(salaryDetailsDTO.getMedicalInsurance());
						saveEmpSalary.setGrossSalary(salaryDetailsDTO.getGrossSalary());
						saveEmpSalary.setNetSalary(salaryDetailsDTO.getNetSalary());
						salaryDetailsRepo.save(saveEmpSalary);

						return new ResponseEntity("EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId()
								+ " is Saved Succesfully", HttpStatus.OK);
				}
			} else {
				return new ResponseEntity(
						"EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId() + " is Not Exist",
						HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {

			e.printStackTrace();
			log.info("e.printStackTrace()---" + e.getMessage());
			return new ResponseEntity(
					"EmployeeSalaryDetails of EmpId:" + salaryDetailsDTO.getEmpId() + " is Not Saved",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public ResponseEntity<String> addAppraisalDetails(AppraisalDetails appraisalDetails) {
		try {
			AppraisalDetails savedAppraisalDetails = appraisalDetailsRepository.save(appraisalDetails);
			return new ResponseEntity<>("AppraisalDetails saved successfully with ID: " + savedAppraisalDetails.getAppr_hist_id(),
					HttpStatus.CREATED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(
					"AppraisalDetails for EmpId:" + appraisalDetails.getEmployee().getId()+ " could not be saved",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}


