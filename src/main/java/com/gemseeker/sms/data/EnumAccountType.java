package com.gemseeker.sms.data;

/**
 *
 * @author gemini1991
 */
public enum EnumAccountType {
    PERSONAL("Personal"), COMMERCIAL("Commercial"), OTHERS("Others");
    
    private final String value;
    
    EnumAccountType(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
