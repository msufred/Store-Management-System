package com.gemseeker.sms.data;

import com.gemseeker.sms.core.data.IEntry;

/**
 *
 * @author gemini1991
 */
public class Service implements IEntry {

    private int serviceId;
    private String name;
    private double estPrice;
    private String description;

    public Service() {
        name = "";
        estPrice = 0.0;
        description = "";
    }
    
    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getEstPrice() {
        return estPrice;
    }

    public void setEstPrice(double estPrice) {
        this.estPrice = estPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `services` (`name`, `est_price`, `description`) VALUES ("
                + "'%s', '%f', '%s')", name, estPrice, description);
    }

    @Override
    public String toString() {
        return name;
    }

}
