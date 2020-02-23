package com.gemseeker.sms.data;

import com.gemseeker.sms.core.data.IEntry;

/**
 *
 * @author gemini1991
 */
public class Product implements IEntry {

    private int productId;
    private String name;
    private double price;
    private int count;
    private String description;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `products` (`name`, `price`, `count`, `description`) "
                + "VALUES ('%s', '%f', '%d', '%s')", name, price, count, description);
    }

    @Override
    public String toString() {
        return name;
    }
}
