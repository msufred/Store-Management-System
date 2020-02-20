package com.gemseeker.sms.core;

import com.gemseeker.sms.core.data.EnumBillingStatus;
import java.util.Date;

/**
 * 
 * @author gemini1991
 */
public interface IAccountBilling {

    void setAccountNo(String accountNo);
    void setDate(Date date); // date of statement/billing
    void setPreviousCharge(double amount, String desc);
    void setCurrentCharge(double amount, String desc);
    double getTotal();
    void setDueDate(Date dueDate);
    void setStatus(EnumBillingStatus billingStatus); // For Payment, Paid or Overdue
    
    String getAccountNo();
    Date getDate();
    double getPreviousChargeAmount();
    String getPreviousChargeDescription();
    double getCurrentChargeAmount();
    String getCurrentChargeDescription();
    Date getDueDate();
    EnumBillingStatus getStatus();
}
