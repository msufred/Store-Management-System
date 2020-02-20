package com.gemseeker.sms.data;

import com.gemseeker.sms.Preferences;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.data.EnumAccountStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
/**
 *
 * @author gemini1991
 */
public class Database {

    private static Database instance;
    private Connection connection;
    private boolean isOpen;
    
    private PreparedStatement getAllAccounts;
    private PreparedStatement getAllBillings;
    
    private Database() throws SQLException {
        Preferences pref = Preferences.getInstance();
        if (!pref.isLoaded()) {
            return;
        }
        
        String url = "jdbc:mysql://" + pref.getDatabaseURL();
        connection = DriverManager.getConnection(
                url,
                pref.getDatabaseUser(),
                pref.getDatabasePassword()
        );
        isOpen = true;
        
        getAllAccounts = connection.prepareCall("SELECT * FROM `accounts`");
        getAllBillings = connection.prepareCall("SELECT * FROM `billings`");
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public boolean isOpen() {
        return isOpen;
    }
    
    public void close() throws SQLException {
        getAllAccounts.close();
        
        connection.close();
    }
    
    public static Database getInstance() throws SQLException {
        if (instance == null) instance = new Database();
        return instance;
    }
    
    /*==================================================================*
    |                                                                   |
    |                               ACCOUNTS                            |
    |                                                                   |
    *===================================================================*/
    
    public int getAccountsCount() {
        int count = -1;
        if (connection != null && isOpen) {
            ResultSet rs = null;
            Statement s = null;
            try {
                s = connection.createStatement();
                rs = s.executeQuery("SELECT COUNT(*) FROM `accounts`");
                rs.next();
                count = rs.getInt(1);
            } catch (SQLException e) {
                System.err.println("Error while query.\n" + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close result set and statement.\n" + ex);
                }
            }
        }
        return count;
    }
    
    public boolean hasAccountNo(String acctNo) {
        int count = 0;
        if (connection != null && isOpen) {
            ResultSet rs = null;
            Statement s = null;
            try {
                s = connection.createStatement();
                rs = s.executeQuery("SELECT COUNT(*) FROM `accounts` WHERE `account_no`=\"" + acctNo + "\"");
                rs.next();
                count = rs.getInt(1);
            } catch (SQLException e) {
                System.err.println("Error while query.\n" + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close result set and statement.\n" + ex);
                }
            }
        }
        return count > 0;
    }
    
    public ArrayList<Account> getAllAccounts() {
        ArrayList<Account> accounts = new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAllAccounts.executeQuery();
                while (rs.next()) {
                    Account account = new Account();
                    account.setAccountNumber(rs.getString(1));
                    account.setFirstName(rs.getString(2));
                    account.setLastName(rs.getString(3));
                    Address address = new Address();
                    address.setStreet(rs.getString(4));
                    address.setBarangay(rs.getString(5));
                    address.setCity(rs.getString(6));
                    account.setAddress(address);
                    account.setContactNumber(rs.getString(7));
                    account.setDataPlan(rs.getInt(8));
                    account.setStatus(EnumAccountStatus.valueOf(rs.getString(9)));
                    account.setDateRegistered(Utils.MYSQL_DATETIME_FORMAT.parse(rs.getString(10)));
                    accounts.add(account);
                }
            } catch (SQLException | ParseException e) {
                System.err.println("Error while fetching accounts from database:\n" + e);
            } finally {
                try {
                    if (rs != null && !rs.isClosed()) rs.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close ResultSet object.\n" + ex);
                }
            }
        }
        return accounts;
    }
    
    //----------
    
    public boolean addAccount(Account account) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(account.generateSQLInsert());
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while adding account entry to database.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close Statement object. \n" + ex);
                }
            }
        }
        return false;
    }
    
    //------------
    
    public Account getAccount(String accountNo) {
        Account account = null;
        if (connection != null && isOpen) {
            Statement s = null;
            ResultSet rs = null;
            try {
                s = connection.createStatement();
                rs = s.executeQuery("SELECT * FROM `accounts` WHERE `account_no`=\"" + accountNo + "\"");
                rs.next();
                account = new Account();
                account.setAccountNumber(rs.getString(1));
                account.setFirstName(rs.getString(2));
                account.setLastName(rs.getString(3));
                Address address = new Address();
                address.setStreet(rs.getString(4));
                address.setBarangay(rs.getString(5));
                address.setCity(rs.getString(6));
                account.setAddress(address);
                account.setContactNumber(rs.getString(7));
                account.setDataPlan(rs.getInt(8));
                account.setStatus(EnumAccountStatus.valueOf(rs.getString(9)));
                account.setDateRegistered(Utils.MYSQL_DATETIME_FORMAT.parse(rs.getString(10)));
            } catch (SQLException | ParseException e) {
                System.err.println("Error while fetching account data.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close Statement and ResultSet objects.\n" + e);
                }
            }
        }
        return account;
    }
    
    /*==================================================================*
    |                                                                   |
    |                               BILLINGS                            |
    |                                                                   |
    *===================================================================*/
    
    public ArrayList<Billing> getAllBillings() {
        ArrayList<Billing> billings =  new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAllBillings.executeQuery();
                while (rs.next()) {
                    Billing billing = new Billing();
                    billing.setBillingId(rs.getInt(1));
                    billing.setAccountNo(rs.getString(2));
                    billing.setMonth(rs.getInt(3));
                    billing.setYear(rs.getInt(4));
                    billing.setDueDate(rs.getString(5));
                    billing.setStatus(rs.getString(6));
                    billings.add(billing);
                }
            } catch (SQLException e) {
                System.err.println("Error while fetching billings from database.\n" + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    System.err.println("Error while closing result set.");
                }
            }
        }
        return billings;
    }
    
    public boolean addBilling(Billing billing) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(billing.generateSQLInsert());
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while adding billing entry to database.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close Statement object. \n" + ex);
                }
            }
        }
        return false;
    }
    
    /*==================================================================*
    |                                                                   |
    |                               PAYMENTS                            |
    |                                                                   |
    *===================================================================*/
}
