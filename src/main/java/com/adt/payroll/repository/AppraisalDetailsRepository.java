package com.adt.payroll.repository;

import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.model.AppraisalDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppraisalDetailsRepository extends JpaRepository<AppraisalDetails, Integer> {

    @Query(value = "SELECT ad.appr_hist_id AS apprHistId, " +
            "ad.emp_id as employeeId, "+
            "ad.year AS year, " +
            "ad.month AS month, " +
            "ad.bonus AS bonus, " +
            "ad.variable AS variable, " +
            "ad.amount AS amount, " +
            "ad.appraisal_date AS appraisalDate, " +
            "ad.salary AS salary, " +
            "CONCAT(u.first_name, ' ', u.last_name) AS name " +
            "FROM payroll_schema.appraisal_historical_details ad " +
            "JOIN user_schema._employee u ON ad.emp_id = u.employee_id " +
            "WHERE (ad.emp_id, ad.appraisal_date) IN " +
            "(SELECT ad2.emp_id, MAX(ad2.appraisal_date) " +
            "FROM payroll_schema.appraisal_historical_details ad2 " +
            "GROUP BY ad2.emp_id)",
            nativeQuery = true)
    List<Object[]> findLatestAppraisalDetails();

}