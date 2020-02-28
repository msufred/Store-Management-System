package com.gemseeker.sms.data;

import com.gemseeker.sms.core.data.IEntry;

/**
 *
 * @author gemini1991
 */
public class User implements IEntry {
    
    private int userId;
    private String userName;
    private String password;
    private String authority;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String generateSQLInsert() {
        return String.format("INSERT INTO `users` (`username`, `password`, `authority`) VALUES ('%s', '%s', '%s')",
                userName, password, authority);
    }

}
