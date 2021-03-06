package com.gemseeker.sms.data;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.data.IEntry;
import java.util.Date;

/**
 *
 * @author gemini1991
 */
public class Expense implements IEntry {
    
    private int expenseNo;
    private Date date;
    private String type;
    private String description;
    private double amount;
    
    public Expense() {
        type = "";
        description = "";
        amount = 0;
    }

    public int getExpenseNo() {
        return expenseNo;
    }

    public void setExpenseNo(int expenseNo) {
        this.expenseNo = expenseNo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? "" : type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `expenses` (`amount`, `type`, `description`, `date`) VALUES ("
                + "'%f', '%s', '%s', '%s')", amount, type, description,
                Utils.MYSQL_DATETIME_FORMAT.format(date));
    }

}
