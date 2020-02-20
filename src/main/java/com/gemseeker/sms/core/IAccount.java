package com.gemseeker.sms.core;

import com.gemseeker.sms.core.data.EnumAccountStatus;
import com.gemseeker.sms.data.Address;
import java.util.Date;

/**
 *
 * @author gemini1991
 */
public interface IAccount {
    void setAccountNumber(String accountNo);
    void setAccountUserName(String lastName, String firstName);
    void setFirstName(String firstName);
    void setLastName(String lastName);
    void setAddress(Address address);
    void setContactNumber(String contact);
    void setDateRegistered(Date date);
    void setDataPlan(int mbps);
    void setStatus(EnumAccountStatus status);
    
    String getAccountNumber();
    String[] getAccountUserName();
    String getFirstName();
    String getLastName();
    Address getAddress();
    String getContactNumber();
    Date getDateRegistered();
    int getDataPlan();
    EnumAccountStatus getStatus();
}
