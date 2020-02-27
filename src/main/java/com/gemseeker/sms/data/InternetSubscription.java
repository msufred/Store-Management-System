package com.gemseeker.sms.data;

import com.gemseeker.sms.core.data.IEntry;

/**
 *
 * @author gemini1991
 */
public class InternetSubscription implements IEntry {
    
    private int subscriptionNo;
    private String accountNo;
    private String ipAddress;
    private int bandwidth;
    private double amount;
    private float longitude;
    private float latitude;
    private float elevation;
    
    public int getSubscriptionNo() {
        return subscriptionNo;
    }
    
    public void setSubscriptionNo(int no) {
        this.subscriptionNo = no;
    }
    
    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getElevation() {
        return elevation;
    }
    
    public void setElevation(float elevation) {
        this.elevation = elevation;
    }
    
    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `internet_subscriptions` ("
                + "`account_no`, `bandwidth`, `amount`, `ip_address`, `longitude`, `latitude`, `elevation`) VALUES ("
                + "'%s', '%d', '%f', '%s', '%f', '%f', '%f')",
                accountNo, bandwidth, amount, ipAddress, longitude, latitude, elevation);
    }

}
