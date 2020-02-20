package com.gemseeker.sms.core.data;

/**
 * IEntry interface represents an entry in the database.
 * 
 * @author gemini1991
 */
public interface IEntry {
    
    /**
     * All entry must generate INSERT SQL command.
     * @return 
     */
    String generateSQLInsert();
}
