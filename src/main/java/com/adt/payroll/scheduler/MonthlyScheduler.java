package com.adt.payroll.scheduler;

import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MonthlyScheduler {

    @Autowired
    private LeaveRepository leaveRepository;

    @Scheduled(cron = "0 0 0 1 * *") // Executes on the 1st day of each month at midnight
    public void updateColumnValue() {
           List<LeaveModel> leaveModelList = leaveRepository.findAll();
           for(LeaveModel lm : leaveModelList){
               lm.setLeaveBalance(lm.getLeaveBalance()+1);
               leaveRepository.save(lm);
           }

    }
}