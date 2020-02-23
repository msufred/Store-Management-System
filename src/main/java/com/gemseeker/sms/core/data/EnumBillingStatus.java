package com.gemseeker.sms.core.data;

/**
 *
 * @author gemini1991
 */
public enum EnumBillingStatus {
    FOR_PAYMENT("For Payment"), PAID("Paid"), OVERDUE("Overdue");
    
    private final String name;
    EnumBillingStatus(String str) {
        this.name = str;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
