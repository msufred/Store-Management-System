package com.gemseeker.sms.data;

import com.gemseeker.sms.AppMain;
import com.gemseeker.sms.Logger;
import com.gemseeker.sms.Preferences;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.data.IEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
/**
 *
 * @author gemini1991
 */
public class Database {

    private static final String DEBUG_NAME = "Database";
    
    private static Database instance;
    private Connection connection;
    private boolean isOpen;
    
    private PreparedStatement getAllUsers;
    private PreparedStatement getAllAccounts;
    private PreparedStatement getAccountsCount;
    private PreparedStatement getAllBillings;
    private PreparedStatement getBillingsCount;
    private PreparedStatement getAllRevenues;
    private PreparedStatement getAllExpenses;
    private PreparedStatement getAllProducts;
    private PreparedStatement getAllServices;
    private PreparedStatement getAllHistory;
    
    private Logger logger;
    
    private Database() throws SQLException {
        logger = AppMain.getLogger();
        
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
        
        getAllUsers = connection.prepareCall("SELECT * FROM `users`");
        getAllAccounts = connection.prepareCall("SELECT * FROM `accounts`");
        getAccountsCount = connection.prepareCall("SELECT COUNT(*) FROM `accounts`");
        getAllBillings = connection.prepareCall("SELECT * FROM `billings`");
        getBillingsCount = connection.prepareCall("SELECT COUNT(*) FROM `billings`");
        getAllRevenues = connection.prepareCall("SELECT * FROM `revenues`");
        getAllExpenses = connection.prepareCall("SELECT * FROM `expenses`");
        getAllProducts = connection.prepareCall("SELECT * FROM `products`");
        getAllServices = connection.prepareCall("SELECT * FROM `services`");
        getAllHistory = connection.prepareCall("SELECT * FROM `histories`");
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public boolean isOpen() {
        return isOpen;
    }
    
    public void close() throws SQLException {
        getAllAccounts.close();
        getAccountsCount.close();
        getAllBillings.close();
        getAllProducts.close();
        getAllServices.close();
        getAllHistory.close();
        connection.close();
        
        getAllAccounts = null;
        getAccountsCount = null;
        getAllBillings = null;
        getAllProducts = null;
        getAllServices = null;
        getAllHistory = null;
        connection = null;
        isOpen = false;
    }
    
    public static Database getInstance() throws SQLException {
        if (instance == null) instance = new Database();
        return instance;
    }
    
    /*==================================================================*
    |                                                                   |
    |                               USERS                               |
    |                                                                   |
    *===================================================================*/
    
    public ArrayList<User> getAllUsers() {
        logger.log(DEBUG_NAME, "fetching all users");
        
        ArrayList<User> users = new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAllUsers.executeQuery();
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt(1));
                    user.setUserName(rs.getString(2));
                    user.setPassword(rs.getString(3));
                    user.setAuthority(rs.getString(4));
                    users.add(user);
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching users from database", e);
            } finally {
                try {
                    if (rs != null && !rs.isClosed()) rs.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing ResultSet object in getAllUsers()", ex);
                }
            }
        }
        return users;
    }
    
    public User getUser(String username, String password) {
        logger.log(DEBUG_NAME, "fetching user with username=" + username + " and password=" + password);
        
        if (connection != null && isOpen) {
            Statement s = null;
            ResultSet rs = null;
            try {
                s = connection.createStatement();
                rs = s.executeQuery("SELECT * FROM `users` WHERE `username`='" +
                        username + "' AND `password`='" + password + "'");
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt(1));
                    user.setUserName(rs.getString(2));
                    user.setPassword(rs.getString(3));
                    user.setAuthority(rs.getString(4));
                    return user;
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching user from database", e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in getUser()", e);
                }
            }
        }
        
        return null;
    }
    
    public boolean addUser(User user) {
        logger.log(DEBUG_NAME, "adding user entry");
        return addEntry(user);
    }
    
    /*==================================================================*
    |                                                                   |
    |                               ACCOUNTS                            |
    |                                                                   |
    *===================================================================*/
    
    public int getAccountsCount() {
        logger.log(DEBUG_NAME, "fetching accounts count");
        
        int count = 0;
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAccountsCount.executeQuery();
                rs.next();
                count = rs.getInt(1);
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching accounts count", e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while close ResultSet object in getAccountsCount()");
                }
            }
        }
        return count;
    }
    
    public int getAccountsCountByStatus(EnumAccountStatus status) {
        logger.log(DEBUG_NAME, "fetching accounts with status=" + status.toString());
        
        if (connection != null && isOpen) {
            ResultSet rs = null;
            Statement s = null;
            try {
                String query = String.format("SELECT COUNT(*) FROM `accounts` WHERE `status` = '%s'", status.toString());
                s = connection.createStatement();
                rs = s.executeQuery(query);
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching accounts with status=" + status.toString(), e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in getAccountsByStatus()", ex);
                }
            }
        }
        return 0;
    }
    
    public boolean hasAccountNo(String acctNo) {
        logger.log(DEBUG_NAME, "checking if account with id=" + acctNo + " exist");
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
                logger.logErr(DEBUG_NAME, "error while checking if account with id=" + acctNo + " exist", e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in hasAccountNo()", ex);
                }
            }
        }
        return count > 0;
    }
    
    public ArrayList<Account> getAllAccounts() {
        logger.log(DEBUG_NAME, "fetching all accounts");
        
        ArrayList<Account> accounts = new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAllAccounts.executeQuery();
                while (rs.next()) {
                    Account account = fetchAccountInfo(rs);
                    accounts.add(account);
                }
            } catch (SQLException | ParseException e) {
                logger.logErr(DEBUG_NAME, "error while fetching all accounts", e);
            } finally {
                try {
                    if (rs != null && !rs.isClosed()) rs.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing ResultSet object in getAllAccounts()", ex);
                }
            }
        }
        return accounts;
    }
    
    //----------
    
    public boolean addAccount(Account account) {
        logger.log(DEBUG_NAME, "adding account entry");
        
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(account.generateSQLInsert());
                if (n > 0) {
                    Address address = account.getAddress();
                    if (address != null) {
                        addAddress(address);
                    }
                    InternetSubscription internet = account.getInternetSubscription();
                    if (internet != null) {
                        addInternetSubscription(internet);
                    }
                }
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while adding account entry", e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in addAccount()", ex);
                }
            }
        }
        return false;
    }
    
    //------------
    
    public Account getAccount(String accountNo) {
        logger.log(DEBUG_NAME, "fetching account with id=" + accountNo);
        
        Account account = null;
        if (connection != null && isOpen) {
            Statement s = null;
            ResultSet rs = null;
            try {
                s = connection.createStatement();
                rs = s.executeQuery("SELECT * FROM `accounts` WHERE `account_no`=\"" + accountNo + "\"");
                if (rs.next()) {
                    account = fetchAccountInfo(rs);
                }
            } catch (SQLException | ParseException e) {
                logger.logErr(DEBUG_NAME, "error while fetching account with id=" + accountNo, e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in getAccount()", e);
                }
            }
        }
        return account;
    }
    
    public boolean deleteAccount(Account account) {
        logger.log(DEBUG_NAME, "deleting account with id=" + account.getAccountNumber());
        
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate("DELETE FROM `accounts` WHERE `account_no`='" + account.getAccountNumber() + "'");
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while deleting account with id=" + account.getAccountNumber(), e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in deleteAccount()", e);
                }
            }
        }
        return false;
    }
    
    private Account fetchAccountInfo(ResultSet rs) throws SQLException, ParseException {
        Account account = new Account();
        account.setAccountNumber(rs.getString(1));
        account.setFirstName(rs.getString(2));
        account.setLastName(rs.getString(3));
        account.setContactNumber(rs.getString(4));
        String statusStr = rs.getString(5);
        if (statusStr.equals(EnumAccountStatus.ACTIVE.toString())) {
            account.setStatus(EnumAccountStatus.ACTIVE);
        } else if (statusStr.equals(EnumAccountStatus.DISCONNECTED.toString())) {
            account.setStatus(EnumAccountStatus.DISCONNECTED);
        } else {
            account.setStatus(EnumAccountStatus.TERMINATED);
        }
        account.setDateRegistered(Utils.MYSQL_DATETIME_FORMAT.parse(rs.getString(6)));
        String lastDatePaidStr = rs.getString(7);
        if (lastDatePaidStr != null) {
            account.setLastDatePaid(Utils.MYSQL_DATETIME_FORMAT.parse(lastDatePaidStr));
        }
        String typeStr = rs.getString(8);
        if (typeStr.equals(EnumAccountType.PERSONAL.toString())) {
            account.setAccountType(EnumAccountType.PERSONAL);
        } else if (typeStr.equals(EnumAccountType.COMMERCIAL.toString())) {
            account.setAccountType(EnumAccountType.COMMERCIAL);
        } else {
            account.setAccountType(EnumAccountType.OTHERS);
        }
        
        // fetch address information
        account.setAddress(getAddress(account.getAccountNumber()));
        
        // fetch internet subscriptions
        account.setInternetSubscription(getInternetSubscription(account.getAccountNumber()));
        
        // fetch balances
        account.setBalances(getBalanceByAccountNo(account.getAccountNumber()));
        
        return account;
    }
    
    /*==================================================================*
    |                                                                   |
    |                               ADDRESS                             |
    |                                                                   |
    *===================================================================*/
    
    public Address getAddress(String acctNo) {
        logger.log(DEBUG_NAME, "fetching address for account with id=" + acctNo);
        
        if (connection != null && isOpen) {
            ResultSet rs = null;
            Statement s = null;
            try {
                s = connection.createStatement();
                rs = s.executeQuery("SELECT * FROM `addresses` WHERE `account_no`='" + acctNo + "'");
                if (rs.next()) {
                    Address address = new Address();
                    address.setAccountNo(rs.getString(1));
                    address.setProvince(rs.getString(2));
                    address.setCity(rs.getString(3));
                    address.setBarangay(rs.getString(4));
                    address.setLandmark(rs.getString(5));
                    return address;
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching address for account with id=" + acctNo, e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in getAddress()", e);
                }
            }
        }
        return null;
    }
    
    public boolean addAddress(Address address) {
        logger.log(DEBUG_NAME, "adding address entry");
        return addEntry(address);
    }

    /*==================================================================*
    |                                                                   |
    |                               BILLINGS                            |
    |                                                                   |
    *===================================================================*/
    
    public int getBillingsCount() {
        logger.log(DEBUG_NAME, "fetching billings count");
        
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getBillingsCount.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching billings count", e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing ResultSet object in getBillingsCount()", ex);
                }
            }
        }
        return 0;
    }
    
    public int getBillingsCountByStatus(EnumBillingStatus status) {
        logger.log(DEBUG_NAME, "fetching billings count with status=" + status.getName());
        
        if (connection != null && isOpen) {
            ResultSet rs = null;
            Statement s = null;
            try {
                String query = String.format("SELECT COUNT(*) FROM `billings` WHERE `status` = '%s'", status.getName());
                s = connection.createStatement();
                rs = s.executeQuery(query);
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching billings count with status=" + status.getName(), e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in getBillingCountByStatus()", ex);
                }
            }
        }
        return 0;
    }
    
    public ArrayList<Billing> getAllBillings() {
        logger.log(DEBUG_NAME, "fetching all billings");
        
        ArrayList<Billing> billings =  new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAllBillings.executeQuery();
                while (rs.next()) {
                    billings.add(fetchBillingInfo(rs));
                }
            } catch (SQLException | ParseException e) {
                logger.logErr(DEBUG_NAME, "error while fetching all billings", e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing ResultSet object in getAllBillings()", e);
                }
            }
        }
        return billings;
    }
    
    public Billing getBilling(int billingNo) {
        logger.log(DEBUG_NAME, "fetching billing with id=" + billingNo);
        
        Billing billing = null;
        if (connection != null && isOpen) {
            Statement s = null;
            ResultSet rs = null;
            try {
                s = connection.createStatement();
                rs = s.executeQuery("SELECT * FROM `billings` WHERE `billing_no`=\"" + billingNo + "\"");
                if (rs.next()) {
                    billing = fetchBillingInfo(rs);
                }
            } catch (SQLException | ParseException e) {
                logger.logErr(DEBUG_NAME, "error while fetching billing with id=" + billingNo, e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in getBilling()", e);
                }
            }
        }
        return billing;
    }
    
    public boolean addBilling(Billing billing) {
        logger.log(DEBUG_NAME, "adding billing entry");
        
        if (connection != null && isOpen) {
            Statement s = null;
            ResultSet rsKeys = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(billing.generateSQLInsert(), Statement.RETURN_GENERATED_KEYS);
                if (n > 0) {
                    rsKeys = s.getGeneratedKeys();
                    if (rsKeys.next()) {
                        int key = rsKeys.getInt(1);
                        for (Payment p : billing.getPayments()) {
                            p.setBillingId(key);
                            addPayment(p);
                        }
                    }
                }
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while adding billing entry", e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rsKeys != null) rsKeys.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in addBilling()", ex);
                }
            }
        }
        return false;
    }
    
    public boolean deleteBilling(Billing billing) {
        logger.log(DEBUG_NAME, "delete billing entry with id=" + billing.getBillingId());
        
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate("DELETE FROM `billings` WHERE `billing_no`='" + billing.getBillingId() + "'");
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while deleting billing entry with id=" + billing.getBillingId(), e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in deleteBilling()", e);
                }
            }
        }
        return false;
    }
    
    public boolean updateBilling(int billingNo, Billing updatedBilling) {
        logger.log(DEBUG_NAME, "updating billing entry with id=" + billingNo);
        
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                StringBuilder sb = new StringBuilder();
                sb.append("UPDATE `billings` SET ")
                        .append("`account_no`='")
                        .append(updatedBilling.getAccountNo())
                        .append("', ")
                        .append("`amount`='")
                        .append(updatedBilling.getAmount())
                        .append("', ")
                        .append("`billing_date`='")
                        .append(Utils.MYSQL_DATETIME_FORMAT.format(updatedBilling.getBillingDate()))
                        .append("', ")
                        .append("`from_date`='")
                        .append(Utils.DATE_FORMAT_1.format(updatedBilling.getFromDate()))
                        .append("', ")
                        .append("`to_date`='")
                        .append(Utils.DATE_FORMAT_1.format(updatedBilling.getToDate()))
                        .append("', ")
                        .append("`due_date`='")
                        .append(updatedBilling.getDueDate())
                        .append("', ")
                        .append("`status`='")
                        .append(updatedBilling.getStatus())
                        .append("', ")
                        .append("`type`='")
                        .append(updatedBilling.getType().getName())
                        .append("', ")
                        
                        // set date_udpated to current date and time
                        .append("`date_updated`='")
                        .append(Utils.MYSQL_DATETIME_FORMAT.format(updatedBilling.getDateUpdated()))
                        .append("'");;
                sb.append(" WHERE (`billing_no`='")
                        .append(billingNo)
                        .append("')");
                int n = s.executeUpdate(sb.toString());
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while updating billing entry with id=" + billingNo, e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in updateBilling()", e);
                }
            }
        }
        return false;
    }
    
    public boolean updateBilling(int billingNo, String columnName, String value) {
        logger.log(DEBUG_NAME, "updating billing entry with id=" + billingNo);
        
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                String sql = String.format("UPDATE `billings` SET "
                        + "`%s` = '%s' WHERE (`billing_no` = '%d')", columnName, value, billingNo);
                int n = s.executeUpdate(sql);
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while updating billing entry with id=" + billingNo, e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in updateBilling()", e);
                }
            }
        }
        return false;
    }
    
    private Billing fetchBillingInfo(ResultSet rs) throws SQLException, ParseException {
        Billing billing = new Billing();
        billing.setBillingId(rs.getInt(1));
        billing.setAccountNo(rs.getString(2));
        billing.setAmount(rs.getDouble(3));
        billing.setBillingDate(Utils.MYSQL_DATETIME_FORMAT.parse(rs.getString(4)));
        String fromStr = rs.getString(5);
        if (fromStr != null) {
            billing.setFromDate(Utils.DATE_FORMAT_1.parse(fromStr));
        }
        String toStr = rs.getString(6);
        if (toStr != null) {
            billing.setToDate(Utils.DATE_FORMAT_1.parse(toStr));
        }
        billing.setDueDate(rs.getString(7));
        
        String statusStr = rs.getString(8);
        if (statusStr.equals(EnumBillingStatus.CANCELLED.toString())) {
            billing.setStatus(EnumBillingStatus.CANCELLED);
        } else if (statusStr.equals(EnumBillingStatus.DELIVERED.toString())) {
            billing.setStatus(EnumBillingStatus.DELIVERED);
        } else if (statusStr.equals(EnumBillingStatus.FOR_DELIVERY.toString())) {
            billing.setStatus(EnumBillingStatus.FOR_DELIVERY);
        } else if (statusStr.equals(EnumBillingStatus.FOR_PAYMENT.toString())) {
            billing.setStatus(EnumBillingStatus.FOR_PAYMENT);
        } else if (statusStr.equals(EnumBillingStatus.OVERDUE.toString())) {
            billing.setStatus(EnumBillingStatus.OVERDUE);
        } else if (statusStr.equals(EnumBillingStatus.PAID.toString())) {
            billing.setStatus(EnumBillingStatus.PAID);
        } else {
            billing.setStatus(EnumBillingStatus.FOR_REVIEW);
        }
        
        String dateUpdatedStr = rs.getString(9);
        if (dateUpdatedStr != null) {
            billing.setDateUpdated(Utils.MYSQL_DATETIME_FORMAT.parse(dateUpdatedStr));
        }
        String type = rs.getString(10);
        if (type.equals(EnumBillingType.WISP.getName())) {
            billing.setType(EnumBillingType.WISP);
        } else {
            billing.setType(EnumBillingType.ITEM);
        }

        billing.setPayments(getPaymentsByBillingId(billing.getBillingId() + ""));

        Account account = getAccount(billing.getAccountNo());
        billing.setAccount(account);
        return billing;
    }
    
    /*==================================================================*
    |                                                                   |
    |                       BILLINGS PROCESSED                          |
    |                                                                   |
    *===================================================================*/
    
    public int addBillingProcessed(BillingProcessed billingProcessed) {
        logger.log(DEBUG_NAME, "adding billing processed entry");
        
        System.out.println("why");
        if (connection != null && isOpen) {
            Statement s = null;
            ResultSet rsKey = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(billingProcessed.generateSQLInsert(), Statement.RETURN_GENERATED_KEYS);
                if (n > 0) {
                    rsKey = s.getGeneratedKeys();
                    if (rsKey.next()) {
                        return rsKey.getInt(1);
                    }
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while updating billing processed entry", e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rsKey != null) rsKey.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in addBillingProcessed()", ex);
                }
            }
        }
        return -1;
    }
    
    /*==================================================================*
    |                                                                   |
    |                               REVENUES                            |
    |                                                                   |
    *===================================================================*/
    
    public ArrayList<Revenue> getAllRevenues() {
        logger.log(DEBUG_NAME, "fetching all revenues");
        
        ArrayList<Revenue> revenues = new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAllRevenues.executeQuery();
                while (rs.next()) {
                    Revenue revenue = new Revenue();
                    revenue.setRevenueNo(rs.getInt(1));
                    revenue.setAmount(rs.getDouble(2));
                    revenue.setType(rs.getString(3));
                    revenue.setDescription(rs.getString(4));
                    revenue.setDate(Utils.MYSQL_DATETIME_FORMAT.parse(rs.getString(5)));
                    revenues.add(revenue);
                }
            } catch (SQLException | ParseException e) {
                logger.logErr(DEBUG_NAME, "error while fetching all revenues", e);
            } finally {
                try {
                    if (rs != null && !rs.isClosed()) rs.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing ResultSet object in getAllRevenues()", ex);
                }
            }
        }
        return revenues; 
    }
    
    public boolean addRevenue(Revenue revenue) {
        logger.log(DEBUG_NAME, "adding revenue entry");
        return addEntry(revenue);
    }
    
    /*==================================================================*
    |                                                                   |
    |                               EXPENSES                            |
    |                                                                   |
    *===================================================================*/
    
    public ArrayList<Expense> getAllExpenses() {
        logger.log(DEBUG_NAME, "fetching expenses");
        
        ArrayList<Expense> expenses = new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAllExpenses.executeQuery();
                while (rs.next()) {
                    Expense expense = new Expense();
                    expense.setExpenseNo(rs.getInt(1));
                    expense.setAmount(rs.getDouble(2));
                    expense.setType(rs.getString(3));
                    expense.setDescription(rs.getString(4));
                    expense.setDate(Utils.MYSQL_DATETIME_FORMAT.parse(rs.getString(5)));
                    expenses.add(expense);
                }
            } catch (SQLException | ParseException e) {
                logger.logErr(DEBUG_NAME, "error while fetching expenses", e);
            } finally {
                try {
                    if (rs != null && !rs.isClosed()) rs.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing ResultSet object in getAllExpenses()", ex);
                }
            }
        }
        return expenses;
    }
    
    public boolean addExpense(Expense expense) {
        logger.log(DEBUG_NAME, "adding expense entry");
        return addEntry(expense);
    }
    
    /*==================================================================*
    |                                                                   |
    |                               BALANCES                            |
    |                                                                   |
    *===================================================================*/
    
    public ArrayList<Balance> getBalanceByAccountNo(String accountNo) {
        logger.log(DEBUG_NAME, "fetching balances for account with id=" + accountNo);
        
        ArrayList<Balance> balances = new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            Statement s = null;
            try {
                s = connection.createStatement();
                rs = s.executeQuery("SELECT * FROM `balances` WHERE `account_no`='" + accountNo + "'");
                while(rs.next()) {
                    Balance b = new Balance();
                    b.setBalanceNo(rs.getInt(1));
                    b.setBillingProcessedNo(rs.getInt(2));
                    b.setAccountNo(rs.getString(3));
                    b.setAmount(rs.getDouble(4));
                    b.setIsPaid(Boolean.parseBoolean(rs.getString(5)));
                    String dateStr = rs.getString(6);
                    if (dateStr != null && !dateStr.isEmpty()) {
                        b.setDatePaid(Utils.MYSQL_DATETIME_FORMAT.parse(dateStr));
                    }
                    balances.add(b);
                }
            } catch (SQLException | ParseException e) {
                logger.logErr(DEBUG_NAME, "error while fetching balances for account with id=" + accountNo, e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rs != null) rs.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in getBalanceByAccountNo", ex);
                }
            }
        }
        return balances;
    }
    
    public boolean addBalance(Balance balance) {
        logger.log(DEBUG_NAME, "adding balance entry");
        return addEntry(balance);
    }
    
    /**
     * Updates the Balance entry. date_paid column is automatically set when calling
     * this method.
     * 
     * @param balanceNo
     * @param paid
     * @return 
     */
    public boolean updateBalance(int balanceNo, boolean paid) {
        logger.log(DEBUG_NAME, "updating balance with id=" + balanceNo);
        
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                String sql = String.format("UPDATE `balances` SET `paid`='%s', `date_paid`='%s' WHERE (`balance_no`='%d')",
                        String.valueOf(paid), Utils.MYSQL_DATETIME_FORMAT.format(Calendar.getInstance().getTime()), balanceNo);
                int n = s.executeUpdate(sql);
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while updating balance with id=" + balanceNo, e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in updateBalance()", e);
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
    
    public ArrayList<Payment> getPaymentsByBillingId(String billingId) {
        logger.log(DEBUG_NAME, "fetching payments of billing with id=" + billingId);
        
        ArrayList<Payment> payments = new ArrayList<>();
        
        if (connection != null && isOpen) {
            String sql = "SELECT * FROM `payments` WHERE `billing_no`='" + billingId +"'";
            ResultSet rs = null;
            Statement s = null;
            try {
                s = connection.createStatement();
                rs = s.executeQuery(sql);
                while (rs.next()) {
                    Payment payment = new Payment();
                    payment.setPaymentId(rs.getInt(1));
                    payment.setBillingId(rs.getInt(2));
                    payment.setName(rs.getString(3));
                    payment.setAmount(rs.getDouble(4));
                    payment.setQuantity(rs.getInt(5));
                    payment.setTotalAmount(rs.getDouble(6));
                    payment.setDescription(rs.getString(7));
                    payments.add(payment);
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching payments for billing with id=" + billingId, e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in getPaymentsByBillingId()", e);
                }
            }
        }
        
        return payments;
    }
    
    public int addPayment(Payment payment) {
        logger.log(DEBUG_NAME, "adding payment entry");
        
        if (connection != null && isOpen) {
            Statement s = null;
            ResultSet rsKey = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(payment.generateSQLInsert(), Statement.RETURN_GENERATED_KEYS);
                if (n > 0) {
                    rsKey = s.getGeneratedKeys();
                    if (rsKey.next()) {
                        return rsKey.getInt(1);
                    }
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while adding payment entry", e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rsKey != null) rsKey.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in addPayment()", ex);
                }
            }
        }
        return -1;
    }
    
    public boolean deletePayment(Payment payment) {
        logger.log(DEBUG_NAME, "deleting payment entry");
        
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate("DELETE FROM `payments` WHERE `payment_no`='" + payment.getPaymentId()+ "'");
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while deleting payment entry", e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in deletePayment()", e);
                }
            }
        }
        return false;
    }
    
    public boolean updatePayment(int paymentId, Payment updatedPayment) {
        logger.log(DEBUG_NAME, "updating payment with id=" + paymentId);
        
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                String sql = String.format("UPDATE `payments` SET "
                        + "`name` = '%s', "
                        + "`amount` = '%f', "
                        + "`quantity` = '%d', "
                        + "`total_amount` = '%f', "
                        + "`description` = '%s' "
                        + "WHERE (`payment_no` = '%d')",
                        updatedPayment.getName(),
                        updatedPayment.getAmount(),
                        updatedPayment.getQuantity(),
                        updatedPayment.getTotalAmount(),
                        updatedPayment.getDescription(),
                        paymentId);
                int n = s.executeUpdate(sql);
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while updating payment with id=" + paymentId, e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in updatePayment()", e);
                }
            }
        }
        return false;
    }
    
    /*==================================================================*
    |                                                                   |
    |                               INTERNET                            |
    |                                                                   |
    *===================================================================*/
    
    public InternetSubscription getInternetSubscription(String acctNo) {
        logger.log(DEBUG_NAME, "fetching internet subscription entry for account with id=" + acctNo);
        
        if (connection != null && isOpen) {
            ResultSet rs = null;
            Statement s = null;
            try {
                s = connection.createStatement();
                String sql = "SELECT * FROM `internet_subscriptions` WHERE `account_no`='" + acctNo + "'";
                rs = s.executeQuery(sql);
                if(rs.next()) {
                    InternetSubscription i = new InternetSubscription();
                    i.setSubscriptionNo(rs.getInt(1));
                    i.setAccountNo(rs.getString(2));
                    i.setBandwidth(rs.getInt(3));
                    i.setAmount(rs.getDouble(4));
                    i.setIpAddress(rs.getString(5));
                    i.setLongitude(rs.getFloat(6));
                    i.setLatitude(rs.getFloat(7));
                    i.setElevation(rs.getFloat(8));
                    return i;
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching internet subscription for account with id=" + acctNo, e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in getInternetSubscription()", e);
                }
            }
        }
        return null;
    }

    public boolean addInternetSubscription(InternetSubscription internet) {
        return addEntry(internet);
    }
    
    /*==================================================================*
    |                                                                   |
    |                               PRODUCTS                            |
    |                                                                   |
    *===================================================================*/
    
    public ArrayList<Product> getAllProducts() {
        logger.log(DEBUG_NAME, "fetching all products");
        
        ArrayList<Product> products = new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAllProducts.executeQuery();
                while(rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt(1));
                    product.setName(rs.getString(2));
                    product.setPrice(rs.getDouble(3));
                    product.setCount(rs.getInt(4));
                    product.setDescription(rs.getString(5));
                    products.add(product);
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching products", e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing ResultSet object in getAllProducts()", e);
                }
            }
        }
        return products;
    }
    
    public boolean addProduct(Product product) {
        logger.log(DEBUG_NAME, "adding product entry");
        return addEntry(product);
    }
    
    public boolean updateProductCount(int productId, int count) {
        logger.log(DEBUG_NAME, "updating count for product with id=" + productId);
        
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                String sql = String.format("UPDATE `products` SET `count` = '%d' WHERE (`product_no` = '%d')", count, productId);
                int n = s.executeUpdate(sql);
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while updating count for product with id=" + productId, e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in updateProductCount()", e);
                }
            }
        }
        return false;
    }
    
    public Product findProductByName(String name) {
        logger.log(DEBUG_NAME, "fetching product with name=" + name);
        
        if (connection != null && isOpen) {
            ResultSet rs = null;
            Statement s = null;
            try {
                s = connection.createStatement();
                rs = s.executeQuery("SELECT * FROM `products` WHERE `name`='" + name + "' LIMIT 1");
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt(1));
                    product.setName(rs.getString(2));
                    product.setPrice(rs.getDouble(3));
                    product.setCount(rs.getInt(4));
                    product.setDescription(rs.getString(5));
                    return product;
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching product with name=" + name, e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement and ResultSet object in findProductByName()", e);
                }
            }
        }
        return null;
    }
    
    /*==================================================================*
    |                                                                   |
    |                               SERVICES                            |
    |                                                                   |
    *===================================================================*/
    
    public ArrayList<Service> getAllServices() {
        logger.log(DEBUG_NAME, "fetching all services");
        
        ArrayList<Service> services = new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAllServices.executeQuery();
                while(rs.next()) {
                    Service service = new Service();
                    service.setServiceId(rs.getInt(1));
                    service.setName(rs.getString(2));
                    service.setEstPrice(rs.getDouble(3));
                    service.setDescription(rs.getString(4));
                    services.add(service);
                }
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while fetching services", e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing ResultSet object in getAllServices*()", e);
                }
            }
        }
        return services;
    }
    
    public boolean addService(Service service) {
        logger.log(DEBUG_NAME, "adding service entry");
        return addEntry(service);
    }
    
    /*==================================================================*
    |                                                                   |
    |                               HISTORY                             |
    |                                                                   |
    *===================================================================*/
    
    public ArrayList<History> getHistories() {
        logger.log(DEBUG_NAME, "fetching histories");
        
        ArrayList<History> histories = new ArrayList<>();
        if (connection != null && isOpen) {
            ResultSet rs = null;
            try {
                rs = getAllHistory.executeQuery();
                while (rs.next()) {
                    History history = new History();
                    history.setId(rs.getInt(1));
                    history.setTitle(rs.getString(2));
                    history.setDescription(rs.getString(3));
                    history.setDate(Utils.MYSQL_DATETIME_FORMAT.parse(rs.getString(4)));
                    histories.add(history);
                }
            } catch (SQLException | ParseException e) {
                logger.logErr(DEBUG_NAME, "error while fetching histories", e);
            } finally {
                try {
                    if (rs != null && !rs.isClosed()) rs.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing ResultSet object in getHistories()", ex);
                }
            }
        }
        return histories;
    }
    
    public boolean addHistory(History history) {
        logger.log(DEBUG_NAME, "adding history entry");
        return addEntry(history);
    }
    
    /*==================================================================*
    |                                                                   |
    |                               GENERAL                             |
    |                                                                   |
    *===================================================================*/
    
    /**
     * Add new entry to the database. Entry must implement IEntry.
     * 
     * @param entry Object that implements IEntry.
     * @return true if added successfully, otherwise returns false
     */
    public boolean addEntry(IEntry entry) {
        logger.log(DEBUG_NAME, "adding entry");
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(entry.generateSQLInsert());
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while adding entry", e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException ex) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in addEntry()", ex);
                }
            }
        }
        return false;
    }
    
    public boolean update(String keyColumn, String keyValue, String table, String column, String value) {
        logger.log(DEBUG_NAME, "updating " + table + " entry");
        
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                String sql = String.format("UPDATE `%s` SET `%s` = '%s' WHERE (`%s` = '%s')",
                        table, column, value, keyColumn, keyValue);
                int n = s.executeUpdate(sql);
                return n > 0;
            } catch (SQLException e) {
                logger.logErr(DEBUG_NAME, "error while updating " + table + " entry", e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    logger.logErr(DEBUG_NAME, "error while closing Statement object in update()", e);
                }
            }
        }
        
        return false;
    }
    
}
