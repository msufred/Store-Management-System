package com.gemseeker.sms.data;

/**
 *
 * @author gemini1991
 */
public class Address {

    private String street, barangay, city;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    
    @Override
    public String toString() {
        return String.format("%s, %s, %s", street, barangay, city);
    }
    
}
