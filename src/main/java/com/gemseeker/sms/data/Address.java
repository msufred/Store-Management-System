package com.gemseeker.sms.data;

import com.gemseeker.sms.core.data.IEntry;

/**
 *
 * @author gemini1991
 */
public class Address implements IEntry {

    private String accountNo;
    private String landmark; // building/lot/street/sitio etc
    private String barangay;
    private String city;
    private String province;
    
    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
    
    public String getAccountNo() {
        return accountNo;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
    
    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s", landmark, barangay, city, province);
    }

    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `addresses` ("
                + "`account_no`, `province`, `city`, `barangay`, `landmark`) VALUES ("
                + "'%s', '%s', '%s', '%s', '%s')", accountNo, province, city, barangay, landmark);
    }
    
}
