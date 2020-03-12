package com.gemseeker.sms.data;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.data.IEntry;
import java.util.Date;

/**
 *
 * @author gemini1991
 */
public class History implements IEntry {

    private int id;
    private String title;
    private String description;
    private Date date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `histories` (`title`, `description`, `date`)"
                + " VALUES ('%s', '%s', '%s')",
                title, description, Utils.MYSQL_DATETIME_FORMAT.format(date));
    }

}
