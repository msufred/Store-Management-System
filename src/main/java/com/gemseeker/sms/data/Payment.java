package com.gemseeker.sms.data;

import com.gemseeker.sms.core.data.IEntry;

/**
 *
 * @author gemini1991
 */
public class Payment implements IEntry {

    private int paymentId;
    private int billingId;
    private String name;
    private double amount;
    private int quantity;
    private double totalAmount;
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
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setTotalAmount(double total) {
        this.totalAmount = total;
    }
    
    public double getTotalAmount() {
        return totalAmount;
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
                + "`name`, "
                + "`amount`, "
                + "`quantity`, "
                + "`total_amount`, "
                + "`description`) VALUES ("
                + "'%d', '%s', '%f', '%d', '%f', '%s')",
                billingId, name,  amount, quantity, totalAmount, description);
    }

}
