package com.gemseeker.sms.data;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.data.IEntry;
import java.util.Date;

/**
 *
 * @author gemini1991
 */
public class Balance implements IEntry {
    
    private int balanceNo;
    private int billingProcessedNo;
    private String accountNo;
    private double amount;
    private boolean isPaid;
    private Date datePaid;
    
    public Balance() {
        isPaid = false;
    }

    public int getBalanceNo() {
        return balanceNo;
    }

    public void setBalanceNo(int balanceNo) {
        this.balanceNo = balanceNo;
    }

    public int getBillingProcessedNo() {
        return billingProcessedNo;
    }

    public void setBillingProcessedNo(int billingNo) {
        this.billingProcessedNo = billingNo;
    }

    public String getAccountNo() {
        return accountNo;
    }
    
    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
    
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isIsPaid() {
        return isPaid;
    }

    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    public Date getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(Date datePaid) {
        this.datePaid = datePaid;
    }
    
    @Override
    public String generateSQLInsert() {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO `balances` (`billing_processed_no`, ");
        if (accountNo != null) {
            sql.append("`account_no`, ");
        }
        sql.append("`amount`, `paid`");
        if (datePaid != null) {
            sql.append(", `date_paid`");
        }
        sql.append(") VALUES (");
        sql.append("'").append(billingProcessedNo).append("', ");
        if (accountNo != null) {
            sql.append("'").append(accountNo).append("', ");
        }
        sql.append("'").append(amount).append("', ");
        sql.append("'").append(String.valueOf(isPaid)).append("'");
        if (datePaid != null) {
            sql.append(", '").append(Utils.MYSQL_DATETIME_FORMAT.format(datePaid)).append("'");
        }
        sql.append(")");
        return sql.toString();
    }

}
