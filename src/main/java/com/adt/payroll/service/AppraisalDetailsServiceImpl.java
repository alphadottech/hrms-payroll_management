package com.adt.payroll.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.dto.MonthSalaryDTO;
import com.adt.payroll.model.AppraisalDetails;
import com.adt.payroll.model.EmpPayrollDetails;
import com.adt.payroll.model.MonthlySalaryDetails;
import com.adt.payroll.model.Reward;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.AppraisalDetailsRepository;
import com.adt.payroll.repository.EmpPayrollDetailsRepo;
import com.adt.payroll.repository.MonthlySalaryDetailsRepo;
import com.adt.payroll.repository.RewardDetailsRepository;
import com.adt.payroll.repository.UserRepo;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AppraisalDetailsServiceImpl implements AppraisalDetailsService,MonthlySalaryService {
    @Autowired
    private AppraisalDetailsRepository appraisalDetailsRepository;

    //@Autowired
    //  private EmployeeRepo employeeRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MonthlySalaryDetailsRepo monthlySalaryDetailsRepo;
    @Autowired
    private RewardDetailsRepository rewardDetailsRepository;
    @Autowired
    EmpPayrollDetailsRepo empPayrollDetailsRepo;

    @Override
    public ResponseEntity<List<AppraisalDetails>> getAppraisalDetails(Integer id) {
        Optional<User> user = userRepo.findByEmployeeId(id);
        if (user.isPresent()) {
            List<AppraisalDetails> appraisalDetailsList = appraisalDetailsRepository.findByEmployee_Id(id);
            appraisalDetailsList.stream().forEach(e -> {
                AppraisalDetailsDTO dto = new AppraisalDetailsDTO();
                dto.setAppraisalDate(e.getAppraisalDate());
                dto.setMonth(e.getMonth());
                dto.setYear(e.getYear());
                dto.setBonus(e.getBonus());
                dto.setAmount(e.getAmount());
                dto.setAppr_hist_id(e.getAppr_hist_id());
                dto.setEmpId(e.getEmpId());
                dto.setVariable(e.getVariable());
                dto.setSalary(e.getSalary());
                dto.setName(e.getEmployee().getFirstName() + " " + e.getEmployee().getLastName());
            });
            if (!appraisalDetailsList.isEmpty()) {
                return ResponseEntity.ok(appraisalDetailsList);
            } else {
                return ResponseEntity.ok(appraisalDetailsList);
            }
        } else {
            throw new EntityNotFoundException("User not found for Employee ID: " + id);
        }
    }

    @Override
    public List<Reward> getRewardDetailsByEmployeeId(Integer id) {
        Optional<User> user = userRepo.findByEmployeeId(id);
        if (user.isPresent()) {
            return rewardDetailsRepository.findByUser_Id(id);
        } else {
            throw new EntityNotFoundException("Employee Not Found");
        }
    }

    @Override
    public String saveProjectRewardDetails(Reward reward) {
        if (!PayrollUtility.validateAmount(reward.getAmount())) {
            throw new EntityNotFoundException("Invalid Amount Details....");
        }
        if (!PayrollUtility.validateType(reward.getRewardType())) {
            throw new EntityNotFoundException("Invalid Reward Type....");
        }
        try {
            Optional<User> userDetails = userRepo.findById(reward.getEmpId());
            reward.setUser(userDetails.get());
            rewardDetailsRepository.save(reward);
            return "Reward details saved successfully";
        } catch (Exception exception) {
            throw new EntityNotFoundException("Employee not found", exception);
        }
    }

	@Override
	public List<MonthSalaryDTO> getAllMonthlySalaryDetails() {
		List<MonthSalaryDTO> monthSalaryResponse = new ArrayList();
		String date = monthlySalaryDetailsRepo.findLatestSalaryCreditedDate();
		Optional<List<MonthlySalaryDetails>> salaryDetails = monthlySalaryDetailsRepo.findByCreditedDate(date);
		if (salaryDetails.isEmpty() || !salaryDetails.isPresent()) {
			return monthSalaryResponse;
		}
		for (MonthlySalaryDetails salaryDetail : salaryDetails.get()) {
			MonthSalaryDTO monthSalaryDTO = new MonthSalaryDTO();
			monthSalaryDTO.setEmp_id(salaryDetail.getEmpId());
			monthSalaryDTO.setEmployeeEsic(salaryDetail.getEmployeeESICAmount());
			monthSalaryDTO.setEmployerEsic(salaryDetail.getEmployerESICAmount());
			monthSalaryDTO.setEmployeePf(salaryDetail.getEmployeeESICAmount());
			monthSalaryDTO.setEmployerPf(salaryDetail.getEmployerPFAmount());
			monthSalaryDTO.setMedicalAmount(salaryDetail.getMedicalInsurance());
			monthSalaryDTO.setNetPay(salaryDetail.getNetSalary());
			Optional<EmpPayrollDetails> empPayrollDetails = empPayrollDetailsRepo
					.findByEmployeeId(salaryDetail.getEmpId());
			monthSalaryDTO.setBankName(empPayrollDetails.get().getBankName());
			monthSalaryDTO.setAccountNo(empPayrollDetails.get().getAccountNumber());
			monthSalaryDTO.setEmployeeName(empPayrollDetails.get().getUser().getFirstName() + " "
					+ empPayrollDetails.get().getUser().getLastName());
			monthSalaryResponse.add(monthSalaryDTO);
		}
		return monthSalaryResponse;
	}

}
