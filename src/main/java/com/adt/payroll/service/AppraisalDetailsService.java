package com.adt.payroll.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.dto.SearchNameDto;
import com.adt.payroll.model.AppraisalDetails;
import com.adt.payroll.model.Reward;

public interface AppraisalDetailsService {
	public ResponseEntity<List<AppraisalDetailsDTO>> getAppraisalDetails(Integer id);
    
    public List<AppraisalDetailsDTO> getRewardDetailsByEmployeeId(Integer id);
    
    public String saveProjectRewardDetails(Reward reward);
    
    public ResponseEntity<String> addAppraisalDetails(AppraisalDetails appraisalDetails);
	
    public ResponseEntity<Page<AppraisalDetailsDTO>> getEmployeesWithLatestAppraisal(int page, int size);
    
    public ResponseEntity<List<SearchNameDto>> getEmployeeNameByCharacter(String nameCharacter);

}
