package com.gemseeker.sms.data;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.data.IEntry;
import java.util.ArrayList;
import java.util.Date;

/**
 * Account entry refers to a registered account that can avail an internet
 * subscription or just to avail certain services.
 *
 * @author gemini1991
 */
public class Account implements IEntry {

    private String accountNumber;
    private String firstName; // name of account user
    private String lastName;
    private String contactNumber; 
    private Date dateRegistered; // start up date - manual
    private Date contractStart;
    private Date contractEnd;
    private EnumAccountStatus status; // current account status
    private Date lastDatePaid;
    private EnumAccountType accountType;
    
    private Address address;
    private InternetSubscription internetSubscription;
    private ArrayList<Balance> balances = new ArrayList<>();
    
    public void setAccountNumber(String accountNo) {
        this.accountNumber = accountNo;
    }
    
    public void setAccountUserName(String lastName, String firstName) {
        this.lastName = lastName;
        this.firstName = firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public void setAddress(Address address) {
        if (address != null) {
            address.setAccountNo(accountNumber);
        }
        this.address = address;
    }

    public void setContactNumber(String contact) {
        this.contactNumber = contact;
    }
    
    public void setDateRegistered(Date date) {
        this.dateRegistered = date;
    }
    
    public void setContractStart(Date date) {
        this.contractStart = date;
    }
    
    public void setContractEnd(Date date) {
        this.contractEnd = date;
    }

    public void setStatus(EnumAccountStatus status) {
        this.status = status;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
    
    public String[] getAccountUserName() {
        return new String[]{firstName, lastName};
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }

    public Address getAddress() {
        return address;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public Date getDateRegistered() {
        return dateRegistered;
    }
    
    public Date getContractStart() {
        return contractStart;
    }
    
    public Date getContractEnd() {
        return contractEnd;
    }

    public EnumAccountStatus getStatus() {
        return status;
    }
    
    public void setLastDatePaid(Date date) {
        lastDatePaid = date;
    }
    
    public Date getLastDatePaid() {
        return lastDatePaid;
    }
        
    public void setInternetSubscription(InternetSubscription sub) {
        if (sub != null) {
            sub.setAccountNo(accountNumber);
        }
        internetSubscription = sub;
    } 
    
    public InternetSubscription getInternetSubscription() {
        return internetSubscription;
    }
    
    public void setAccountType(EnumAccountType acctType) {
        this.accountType = acctType;
    }
    
    public EnumAccountType getAccountType() {
        return accountType;
    }
    
    public void setBalances(ArrayList<Balance> balances) {
        this.balances = balances;
    }
    
    public ArrayList<Balance> getBalances() {
        return balances;
    }
    
    @Override
    public String generateSQLInsert() {
        StringBuilder sb = new StringBuilder(
            "INSERT INTO `accounts` ("
                    + "`account_no`, "      // 1
                    + "`firstname`, "       // 2
                    + "`lastname`, "        // 3
                    + "`contact_no`, "      // 4
                    + "`status`, "          // 5
                    + "`date_registered`, " // 6
        );
        if (lastDatePaid != null) {
            sb.append("`last_date+paid`, ");
        }
        sb.append("`type`");
        if (contractStart != null) {
            sb.append(", `contract_start`");
        }
        if (contractEnd != null) {
            sb.append(", `contract_end`");
        }
        sb.append(") VALUES (");
        sb.append("'").append(accountNumber).append("', ");
        sb.append("'").append(firstName).append("', ");
        sb.append("'").append(lastName).append("', ");
        sb.append("'").append(contactNumber).append("', ");
        sb.append("'").append(status.toString()).append("', ");
        sb.append("'").append(Utils.MYSQL_DATETIME_FORMAT.format(dateRegistered)).append("', ");
        if (lastDatePaid != null) {
            sb.append("'").append(Utils.MYSQL_DATETIME_FORMAT.format(lastDatePaid)).append("', ");
        }
        sb.append("'").append(accountType.toString()).append("'");
        if (contractStart != null) {
            sb.append(", '").append(Utils.MYSQL_DATETIME_FORMAT.format(contractStart)).append("'");
        }
        if (contractEnd != null) {
            sb.append(", '").append(Utils.MYSQL_DATETIME_FORMAT.format(contractEnd)).append("'");
        }
        sb.append(")");

        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("(%s) %s", accountNumber, String.format("%s %s", firstName, lastName));
    }
}
