package com.adt.payroll.service;

public class PayrollUtility {
    public static boolean validateAmount(double lastCTC) {
        if (Double.isNaN(lastCTC) || lastCTC < 0) {
            return false;
        }
        return true;
    }

    public static boolean validateType(String str){
        if(str=="" || str==null || str.matches(".*\\d.*")){
            return false;
        }
        return true;
        }
    }


