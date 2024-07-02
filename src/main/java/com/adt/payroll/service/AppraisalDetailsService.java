package com.adt.payroll.service;

import com.adt.payroll.model.AppraisalDetails;
import com.adt.payroll.model.Reward;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AppraisalDetailsService {
    ResponseEntity<List<AppraisalDetails>> getAppraisalDetails(Integer id);

    List<Reward> getRewardDetailsByEmployeeId(Integer id);

    String saveProjectRewardDetails(Reward reward);
}
