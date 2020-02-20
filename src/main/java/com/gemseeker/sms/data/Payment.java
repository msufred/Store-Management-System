package com.gemseeker.sms.data;

import com.gemseeker.sms.core.data.IEntry;

/**
 *
 * @author gemini1991
 */
public class Payment implements IEntry {

    private int paymentId;
    private int billingId;
    private double amount;
    private String description;

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getBillingId() {
        return billingId;
    }

    public void setBillingId(int billingId) {
        this.billingId = billingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `payments` ("
                + "`billing_no`, "
                + "`amount`, "
                + "`description`) VALUES ("
                + "'%d', %f, %s)",
                billingId, amount, description);
    }

}
