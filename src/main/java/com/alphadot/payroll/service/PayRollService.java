package com.alphadot.payroll.service;

import com.alphadot.payroll.model.PaySlip;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public interface PayRollService {
    public PaySlip createPaySlip(int empId, String month, String year)  throws ParseException, IOException, SQLException ;


    String generatePaySlip(MultipartFile file) throws IOException, ParseException ;
}
