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
    private double amount;
    private Date billingDate;
    private Date fromDate;
    private Date toDate;
    private String dueDate;
    private EnumBillingStatus status;
    private Date dateUpdated;
    private EnumBillingType type;
    
    private Account account;
    private ArrayList<Payment> payments;

    public Billing() {
        payments = new ArrayList<>();
    }
    
    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public Date getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(Date billingDate) {
        this.billingDate = billingDate;
    }

    public EnumBillingStatus getStatus() {
        return status;
    }

    public void setStatus(EnumBillingStatus status) {
        this.status = status;
    }

    public int getBillingId() {
        return billingId;
    }

    public void setBillingId(int billingId) {
        this.billingId = billingId;
    }

    public void setFromDate(Date date) {
        this.fromDate = date;
    }
    
    public Date getFromDate() {
        return fromDate;
    }
    
    public void setToDate(Date date) {
        this.toDate = date;
    }
    
    public Date getToDate() {
        return toDate;
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
    
    public void setType(EnumBillingType type) {
        this.type = type;
    }
    
    public EnumBillingType getType() {
        return type;
    }
    
    public void setPayments(ArrayList<Payment> payments) {
        if (payments != null) {
            for (Payment p : payments) {
                p.setBillingId(billingId);
            }
        }
        this.payments = payments;
        calculateAmount();
    }
    
    public ArrayList<Payment> getPayments() {
        return payments;
    }
    
    public void addPayment(Payment payment) {
        if (payments == null) payments = new ArrayList<>();
        if (payment != null) payment.setBillingId(billingId);
        payments.add(payment);
        calculateAmount();
    }
    
    public void updatePayment(Payment updatedPayment) {
        if (updatedPayment != null) {
            if(payments != null) {
                for (int i = 0; i < payments.size(); i++) {
                    Payment p = payments.get(i);
                    if (p.getPaymentId() == updatedPayment.getPaymentId()) {
                        payments.remove(i);
                        payments.add(i, updatedPayment);
                        break;
                    }
                }
            }
        }
        calculateAmount();
    }
    
    public void removePayment(Payment payment) {
        if (payments != null) {
            payments.remove(payment);
            calculateAmount();
        }
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
    
    /**
     * Calculates the total amount for this billing entry. This method should be
     * called in addPayment, removePayment and updatePayment methods of this
     * Billing class.
     */
    private void calculateAmount() {
        double total = 0;
        if (payments != null) {
            for (Payment p : payments) {
                total += p.getTotalAmount();
            }
        }
        setAmount(total);
    }
    
    @Override
    public String generateSQLInsert() {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO `billings` (`account_no`, `amount`, `billing_date`, ");
        if (fromDate != null) sql.append("`from_date`, ");
        if (toDate != null) sql.append("`to_date`, ");
        sql.append("`due_date`, `status`, ");
        if (dateUpdated != null) {
            sql.append("`date_updated`, ");
        }
        sql.append("`type`").append(") VALUES ('")
                .append(accountNo).append("', '")
                .append(amount).append("', '")
                .append(Utils.MYSQL_DATETIME_FORMAT.format(billingDate)).append("', '");
        if (fromDate != null) {
            sql.append(Utils.DATE_FORMAT_1.format(fromDate)).append("', '");
        }
        if(toDate != null) {
            sql.append(Utils.DATE_FORMAT_1.format(toDate)).append("', '");
        }
        sql.append(dueDate).append("', '").append(status).append("', '");
        if (dateUpdated != null) {
            sql.append(Utils.MYSQL_DATETIME_FORMAT.format(dateUpdated)).append("', '");
        }
        sql.append(type.getName()).append("')");
        
        return sql.toString();
    }

}
