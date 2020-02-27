package com.gemseeker.sms.data;

/**
 *
 * @author gemini1991
 */
public enum EnumBillingType {
    WISP("WISP"), ITEM("ITEM");
    
    private final String name;
    EnumBillingType(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public String getName() {
        return name;
    }
}
