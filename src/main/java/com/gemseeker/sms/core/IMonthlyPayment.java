package com.gemseeker.sms.core;

import java.util.Date;

/**
 *
 * @author gemini1991
 */
public interface IMonthlyPayment {

    void setAccountNo(String acctNo);
    void setDate(Date date);
    void setAmount(double amount);
    void setRemarks(String desc);
    
}
