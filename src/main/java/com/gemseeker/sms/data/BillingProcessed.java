package com.gemseeker.sms.data;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.data.IEntry;
import java.util.Date;

/**
 *
 * @author gemini1991
 */
public class BillingProcessed implements IEntry {

    private int id;
    private int billingNo;
    private double amountDue;
    private double amountPaid;
    private Date dateOfTransaction;
    private String remarks;
    
    private Billing billing;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBillingNo() {
        return billingNo;
    }

    public void setBillingNo(int billingNo) {
        this.billingNo = billingNo;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Date getDateOfTransaction() {
        return dateOfTransaction;
    }

    public void setDateOfTransaction(Date dateOfTransaction) {
        this.dateOfTransaction = dateOfTransaction;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public void setBilling(Billing billing) {
        this.billing = billing;
    }
    
    public Billing getBilling() {
        return billing;
    }

    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `billings_processed` ("
                + "`billing_no`, `amount_due`, `amound_paid`, `date_transaction`, `remarks`) "
                + "VALUES ('%d', '%f', '%f', '%s', '%s')", 
                billingNo, amountDue, amountPaid, Utils.MYSQL_DATETIME_FORMAT.format(dateOfTransaction), remarks);
    }
    
}
