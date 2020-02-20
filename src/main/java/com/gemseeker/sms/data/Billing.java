package com.gemseeker.sms.data;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.data.IEntry;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author gemini1991
 */
public class Billing implements IEntry {

    private int billingId;
    private String accountNo;
    private Date billingDate;
    private int month;
    private int year;
    private String dueDate;
    private String status;
    private Date dateUpdated;
    
    private ArrayList<Payment> payments;

    public Date getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(Date billingDate) {
        this.billingDate = billingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBillingId() {
        return billingId;
    }

    public void setBillingId(int billingId) {
        this.billingId = billingId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    
    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
    
    public Date getDateUpdated() {
        return dateUpdated;
    }
    
    public void setPayments(ArrayList<Payment> payments) {
        this.payments = payments;
    }
    
    public ArrayList<Payment> getPayments() {
        return payments;
    }

    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `billings` ("
                + "`account_no`, `month`, `year`, `due_date`, `status`, `date_updated`) VALUES ("
                + "'%s', '%d', '%d', '%s', '%s', '%s')",
                accountNo, month, year, dueDate, status, Utils.MYSQL_DATETIME_FORMAT.format(dueDate));
    }

}
