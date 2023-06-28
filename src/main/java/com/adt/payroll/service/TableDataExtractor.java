package com.adt.payroll.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TableDataExtractor {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TableDataExtractor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> extractDataFromTable(String sql) {

        return jdbcTemplate.queryForList(sql);
    }
}