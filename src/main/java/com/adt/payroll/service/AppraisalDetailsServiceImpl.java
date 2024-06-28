package com.adt.payroll.service;

import com.adt.payroll.model.AppraisalDetails;
import com.adt.payroll.model.Reward;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.AppraisalDetailsRepository;
import com.adt.payroll.repository.EmployeeRepo;
import com.adt.payroll.repository.RewardDetailsRepository;
import com.adt.payroll.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppraisalDetailsServiceImpl implements AppraisalDetailsService {
    @Autowired
    private AppraisalDetailsRepository appraisalDetailsRepository;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    RewardDetailsRepository rewardDetailsRepository;

    @Override
    public ResponseEntity<AppraisalDetails> getAppraisalDetails(Integer id) {
        Optional<AppraisalDetails> appraisalDetails = appraisalDetailsRepository.findById(id);
        return appraisalDetails.map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Appraisal Details Not Found"));
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
        if(!PayrollUtility.validateAmount(reward.getAmount())){
            throw new EntityNotFoundException("Invalid Amount Details....");
        }
        if(!PayrollUtility.validateType(reward.getRewardType())){
            throw new EntityNotFoundException("Invalid Reward Type....");
        }
        try{
            Optional<User> userDetails = userRepo.findById(reward.getUser().getId());
            reward.setUser(userDetails.get());
            rewardDetailsRepository.save(reward);
            return "Reward details saved successfully";
        }
        catch(Exception exception){

            throw new EntityNotFoundException("Employee not found",exception);
        }
    }
}
