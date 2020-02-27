package com.gemseeker.sms.data;

/**
 *
 * @author gemini1991
 */
public enum EnumBillingStatus {
    // wisp type status
    FOR_REVIEW("For Review"), 
    FOR_PAYMENT("For Payment"), 
    PAID("Paid"), 
    OVERDUE("Overdue"), 
    
    // item type status
    FOR_DELIVERY("For Delivery"),
    CANCELLED("Cancelled"),
    DELIVERED("Delivered");
    
    private final String name;
    EnumBillingStatus(String str) {
        this.name = str;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public String getName() {
        return name;
    }
}
