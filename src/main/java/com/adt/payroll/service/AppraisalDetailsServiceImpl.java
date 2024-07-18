package com.adt.payroll.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.adt.payroll.dto.RewardDetailsDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.dto.SalaryDTO;
import com.adt.payroll.dto.SearchNameDto;
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
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AppraisalDetailsRepository appraisalDetailsRepository;
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
			monthSalaryDTO.setMonth(salaryDetail.getMonth());
			monthSalaryDTO.setYear(salaryDetail.getCreditedDate().substring(salaryDetail.getCreditedDate().length() - 4));
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
    
    @Override
	public ResponseEntity<List<SearchNameDto>> getEmployeeNameByCharacter(String nameCharacter) {
		try {
			LOGGER.info("getEmployeeNameByCharacter started :");
			String firstName ="";
			String lastName ="";
			if(nameCharacter.contains(" ")) {
				String [] ar =nameCharacter.split(" ");
				firstName=ar[0];
				lastName=ar[1];
			}else {
				firstName=nameCharacter;
			}
				
		    List<User> users= userRepo.findByFirstNameAndLastName(firstName, lastName);

			List<SearchNameDto> nameList = new ArrayList();
			if (!users.isEmpty()) {
				
				users.stream().forEach(u -> {
					SearchNameDto dto = new SearchNameDto();
					String user = u.getFirstName() + " " + u.getLastName();
					dto.setEmpId(u.getId());
					dto.setName(user);
					nameList.add(dto);
				});
			}
			return new ResponseEntity<List<SearchNameDto>>(nameList, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.info("Getting error while fetching data from db in getEmployeeNameByCharacter: ", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
    
    @Override
	public ResponseEntity<String> addAppraisalDetails(AppraisalDetails appraisalDetails) {
    	LOGGER.info("Adding appraisal details for Employee ID: {}", appraisalDetails.getEmpId());
		if (appraisalDetails.getEmpId() == null ) {
			LOGGER.error("Employee Id can not be null.");
			return new ResponseEntity<>("Employee Id can not be null ", HttpStatus.BAD_REQUEST);
		}

		if (appraisalDetails.getSalary() == null || appraisalDetails.getSalary() < 0) {
			LOGGER.error("Salary must be specified and must be non-negative.");
			return new ResponseEntity<>("Salary must be specified and must be non-negative.", HttpStatus.BAD_REQUEST);
		}
		if (appraisalDetails.getAmount() == null || appraisalDetails.getAmount() < 0) {
			LOGGER.error("Amount must be specified and must be non-negative.");
			return new ResponseEntity<>("Amount must be specified and must be non-negative.", HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<User> employeeOptional = userRepo.findById(appraisalDetails.getEmpId());
			if (employeeOptional.isEmpty()) {
				LOGGER.warn("Employee with ID: " + appraisalDetails.getEmpId() + " not found");
				return new ResponseEntity<>("Employee with ID: " + appraisalDetails.getEmpId() + " not found",
						HttpStatus.NOT_FOUND);
			}
			AppraisalDetails savedAppraisalDetails = appraisalDetailsRepository.save(appraisalDetails);
			return new ResponseEntity<>("AppraisalDetails saved successfully with ID: " + savedAppraisalDetails.getAppr_hist_id(),
					HttpStatus.CREATED);

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Failed to save AppraisalDetails for EmpId: " + appraisalDetails.getEmpId());
			return new ResponseEntity<>("AppraisalDetails for EmpId: " + appraisalDetails.getEmpId() + " could not be saved",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@Override
	public ResponseEntity<Page<AppraisalDetailsDTO>> getEmployeesWithLatestAppraisal(int page, int size) {
		LOGGER.info("Getting all employees with latest appraisal details");

		List<AppraisalDetailsDTO> appraisalDetailsDTOList = new ArrayList<>();

		try {

			List<Object[]> latestAppraisalResults = appraisalDetailsRepository.findLatestAppraisalDetails();
			if (latestAppraisalResults != null && !latestAppraisalResults.isEmpty()) {
				for (Object[] result : latestAppraisalResults) {
					AppraisalDetailsDTO appraisalDetailsDTO = new AppraisalDetailsDTO();
					appraisalDetailsDTO.setAppr_hist_id((Integer) result[0]);
					appraisalDetailsDTO.setEmpId((int) result[1]);
					appraisalDetailsDTO.setYear(String.valueOf(result[2]));
					appraisalDetailsDTO.setMonth(String.valueOf(result[3]));
					appraisalDetailsDTO.setBonus(result[4] != null ? (Double) result[4] : 0.0);
					appraisalDetailsDTO.setVariable(result[5] != null ? (Double) result[5] : 0.0);
					appraisalDetailsDTO.setAmount((Double) result[6]);
					appraisalDetailsDTO.setAppraisalDate(String.valueOf(result[7]));
					appraisalDetailsDTO.setSalary((Double) result[8]);
					appraisalDetailsDTO.setName((String) result[9]);

					appraisalDetailsDTOList.add(appraisalDetailsDTO);
				}
			} else {
				LOGGER.warn("No employee with latest appraisal details found");
			}
		}
		catch (Exception e) {
			LOGGER.error("Failed to retrieve employees with latest appraisal details", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
		}


		try {
			List<Object[]> employeesWithoutAppraisalResults = appraisalDetailsRepository.findEmployeesWithoutAppraisal();
			if (employeesWithoutAppraisalResults != null && !employeesWithoutAppraisalResults.isEmpty()) {
				for (Object[] result : employeesWithoutAppraisalResults) {
					AppraisalDetailsDTO appraisalDetailsDTO = new AppraisalDetailsDTO();
					appraisalDetailsDTO.setName((String) result[0]);
					appraisalDetailsDTO.setEmpId(Integer.parseInt(String.valueOf(result[1])));
					appraisalDetailsDTO.setAppraisalDate(String.valueOf(result[2]));
					appraisalDetailsDTO.setSalary((Double) result[3]);
					appraisalDetailsDTO.setVariable(result[4] != null ? (Double) result[4] : 0.0);
					appraisalDetailsDTO.setBonus(result[5] != null ? (Double) result[5] : 0.0);
					appraisalDetailsDTO.setAmount(0.0);

					appraisalDetailsDTOList.add(appraisalDetailsDTO);
				}
			} else {
				LOGGER.warn("No employees without appraisal details found");
			}
		}
		catch (Exception e) {
			LOGGER.error("Failed to retrieve employees without latest appraisal details", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
		}
		if (appraisalDetailsDTOList.isEmpty()) {
			LOGGER.warn("No employees found");
			return new ResponseEntity<>(Page.empty(), HttpStatus.OK);
		}


		int start = Math.min((int) PageRequest.of(page, size).getOffset(), appraisalDetailsDTOList.size());
		int end = Math.min((start + size), appraisalDetailsDTOList.size());
		Page<AppraisalDetailsDTO> pageResult = new PageImpl<>(appraisalDetailsDTOList.subList(start, end), PageRequest.of(page, size), appraisalDetailsDTOList.size());

		return new ResponseEntity<>(pageResult, HttpStatus.OK);
	}
}
