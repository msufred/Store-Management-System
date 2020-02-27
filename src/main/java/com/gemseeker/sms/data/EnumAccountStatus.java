package com.gemseeker.sms.data;

/**
 *
 * @author gemini1991
 */
public enum EnumAccountStatus {
    ACTIVE("Active"), DISCONNECTED("Disconnected"), TERMINATED("Terminated");
    
    private final String value;
    
    EnumAccountStatus(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
