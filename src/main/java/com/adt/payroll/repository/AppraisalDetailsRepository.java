package com.adt.payroll.repository;

import com.adt.payroll.dto.AppraisalDetailsDTO;
import com.adt.payroll.model.AppraisalDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppraisalDetailsRepository extends JpaRepository<AppraisalDetails, Integer> {

    @Query(value = "SELECT DISTINCT ON (ad.emp_id) " +
            "ad.appr_hist_id AS apprHistId, " +
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
            "GROUP BY ad2.emp_id) ",
            nativeQuery = true)
    List<Object[]> findLatestAppraisalDetails();

    @Query(value = "SELECT CONCAT(e.first_name, ' ', e.last_name) AS name, " +
            "e.employee_id, "+
            "ep.joining_date, " +
            "ep.salary, " +
            "ep.variable_amount, " +
            "sd.bonus " +
            "FROM user_schema._employee e " +
            "LEFT JOIN payroll_schema.emp_payroll_details ep ON e.employee_id = ep.emp_id " +
            "LEFT JOIN payroll_schema.salary_details sd ON e.employee_id = sd.emp_id " +
            "WHERE e.employee_id NOT IN " +
            "(SELECT ad.emp_id " +
            "FROM payroll_schema.appraisal_historical_details ad)",
            nativeQuery = true)
    List<Object[]> findEmployeesWithoutAppraisal();

}