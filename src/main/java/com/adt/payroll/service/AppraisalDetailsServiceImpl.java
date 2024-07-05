package com.adt.payroll.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.adt.payroll.dto.RewardDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.dto.SalaryDTO;
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
    public ResponseEntity<List<AppraisalDetailsDTO>> getAppraisalDetails(Integer id) {
        Optional<User> user = userRepo.findByEmployeeId(id);
        if (user.isPresent()) {
            List<AppraisalDetailsDTO> appraisalDetailsDTOS =new ArrayList<>();
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
                appraisalDetailsDTOS.add(dto);
            });
            if (!appraisalDetailsList.isEmpty()) {
                return ResponseEntity.ok(appraisalDetailsDTOS);
            } else {
                return ResponseEntity.ok(appraisalDetailsDTOS);
            }
        } else {
            throw new EntityNotFoundException("User not found for Employee ID: " + id);
        }
    }

    @Override
    public List<AppraisalDetailsDTO> getRewardDetailsByEmployeeId(Integer id) {
        Optional<User> userOptional = userRepo.findByEmployeeId(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Reward> rewards = rewardDetailsRepository.findByUser_Id(id);
            List<AppraisalDetailsDTO> appraisalDetailsDTOs = new ArrayList<>();
            for (Reward reward : rewards) {
                AppraisalDetailsDTO appraisalDetailsDTO = new AppraisalDetailsDTO();
                appraisalDetailsDTO.setEmpId(reward.getEmpId());
                appraisalDetailsDTO.setAmount(reward.getAmount());
                appraisalDetailsDTO.setAppraisalDate(reward.getEffectiveDate());
                appraisalDetailsDTO.setRewardType(reward.getRewardType());
                appraisalDetailsDTO.setName(user.getFirstName()+" "+user.getLastName());
                appraisalDetailsDTOs.add(appraisalDetailsDTO);
            }
            return appraisalDetailsDTOs;
        } else {
            throw new EntityNotFoundException("Employee Not Found");
        }
    }


    @Override
    public String saveProjectRewardDetails(Reward reward) {
        try {
            if (!PayrollUtility.validateAmount(reward.getAmount())) {
                throw new IllegalArgumentException("Invalid Amount Details....");
            }
            if (!PayrollUtility.validateType(reward.getRewardType())) {
                throw new IllegalArgumentException("Invalid Reward Type....");
            }
            Optional<User> userDetails = userRepo.findById(reward.getEmpId());
            if (!userDetails.isPresent()) {
                throw new EntityNotFoundException("Employee Not Found");
            }
            if (reward.getId() != null) {
                throw new IllegalArgumentException("ID must be null for new rewards");
            }
            Reward savedReward = rewardDetailsRepository.save(reward);
            return "Reward details saved successfully with ID: " + savedReward.getId();
        } catch (IllegalArgumentException | EntityNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to save reward details: " + ex.getMessage());
        }
    }


	@Override
	public List<SalaryDTO> getAllMonthlySalaryDetails() {
		List<SalaryDTO> monthSalaryResponse = new ArrayList();
		String date = monthlySalaryDetailsRepo.findLatestSalaryCreditedDate();
		Optional<List<MonthlySalaryDetails>> salaryDetails = monthlySalaryDetailsRepo.findByCreditedDate(date);
		if (salaryDetails.isEmpty() || !salaryDetails.isPresent()) {
			return monthSalaryResponse;
		}
		for (MonthlySalaryDetails salaryDetail : salaryDetails.get()) {
			SalaryDTO monthSalaryDTO = new SalaryDTO();
			monthSalaryDTO.setEmpId(salaryDetail.getEmpId());
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
    public ByteArrayInputStream getExcelData(Integer empId) throws IOException {
        List<MonthlySalaryDetails> list=monthlySalaryDetailsRepo.findSalaryDetailsByEmpId(empId);
        Optional<EmpPayrollDetails> list1=empPayrollDetailsRepo.findByEmployeeId(empId);
        ByteArrayInputStream byteArrayInputStream= MonthlySalaryHelper.dataToExcel(list,list1);
        return byteArrayInputStream;
    }

}
