package com.gemseeker.sms.data;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.IAccount;
import com.gemseeker.sms.core.data.EnumAccountStatus;
import com.gemseeker.sms.core.data.IEntry;
import java.util.Date;

/**
 *
 * @author gemini1991
 */
public class Account implements IAccount, IEntry {

    private String accountNumber;
    private String firstName; // name of account user
    private String lastName;
    private Address address;
    private String contactNumber; 
    private Date dateRegistered;
    private EnumAccountStatus status; // current account status
    
    // custom variables (internet provider)
    private int dataPlan; // i.e., 10 mbps=10
    private Date dueDate;
    private Date lastDatePaid;

    @Override
    public void setAccountNumber(String accountNo) {
        this.accountNumber = accountNo;
    }
    
    @Override
    public void setAccountUserName(String lastName, String firstName) {
        this.lastName = lastName;
        this.firstName = firstName;
    }
    
    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Override
    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public void setContactNumber(String contact) {
        this.contactNumber = contact;
    }
    
    @Override
    public void setDateRegistered(Date date) {
        this.dateRegistered = date;
    }

    @Override
    public void setDataPlan(int mbps) {
        this.dataPlan = mbps;
    }

    @Override
    public void setStatus(EnumAccountStatus status) {
        this.status = status;
    }

    @Override
    public String getAccountNumber() {
        return accountNumber;
    }
    
    @Override
    public String[] getAccountUserName() {
        return new String[]{firstName, lastName};
    }
    
    @Override
    public String getFirstName() {
        return firstName;
    }
    
    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public Address getAddress() {
        return address;
    }
    
    @Override
    public String getContactNumber() {
        return contactNumber;
    }
    
    @Override
    public Date getDateRegistered() {
        return dateRegistered;
    }

    @Override
    public int getDataPlan() {
        return dataPlan;
    }

    @Override
    public EnumAccountStatus getStatus() {
        return status;
    }
    
    public void setLastDatePaid(Date date) {
        lastDatePaid = date;
    }
    
    public Date getLastDatePaid() {
        return lastDatePaid;
    }

    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `accounts` ("
                + "`account_no`, "      // 1
                + "`firstname`, "       // 2
                + "`lastname`, "        // 3
                + "`street`, "          // 4
                + "`barangay`, "        // 5
                + "`city`, "            // 6
                + "`contact_no`, "      // 7
                + "`data_plan`, "       // 8
                + "`status`, "          // 9
                + "`date_registered`" // 10
                + ") VALUES ("
                + "'%s', "              // 1
                + "'%s', "              // 2
                + "'%s', "              // 3
                + "'%s', "              // 4
                + "'%s', "              // 5
                + "'%s', "              // 6
                + "'%s', "              // 7
                + "'%s', "              // 8
                + "'%s', "              // 9
                + "'%s'"              // 10
                + ")",
                accountNumber, firstName, lastName, address.getStreet(),
                address.getBarangay(), address.getCity(), contactNumber, dataPlan,
                String.valueOf(status), Utils.MYSQL_DATETIME_FORMAT.format(dateRegistered));
    }
    
    @Override
    public String toString() {
        return String.format("(%s) %s", accountNumber, String.format("%s %s", firstName, lastName));
    }
}
