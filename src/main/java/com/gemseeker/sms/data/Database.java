package com.gemseeker.sms.data;

import com.gemseeker.sms.Preferences;
import com.gemseeker.sms.Utils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
/**
 *
 * @author gemini1991
 */
public class Database {

    private static Database instance;
    private Connection connection;
    private boolean isOpen;
    
    private PreparedStatement getAllAccounts;
    private PreparedStatement getAccountsCount;
    private PreparedStatement getAllBillings;
    private PreparedStatement getAllProducts;
    private PreparedStatement getAllServices;
    
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
        getAccountsCount = connection.prepareCall("SELECT COUNT(*) FROM `accounts`");
        getAllBillings = connection.prepareCall("SELECT * FROM `billings`");
        getAllProducts = connection.prepareCall("SELECT * FROM `products`");
        getAllServices = connection.prepareCall("SELECT * FROM `services`");
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
        
        connection.close();
        
        getAllAccounts = null;
        getAccountsCount = null;
        getAllBillings = null;
        getAllProducts = null;
        connection = null;
        isOpen = false;
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
            try {
                rs = getAccountsCount.executeQuery();
                rs.next();
                count = rs.getInt(1);
            } catch (SQLException e) {
                System.err.println("Error while query.\n" + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close ResultSet object.\n" + ex);
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
                    Account account = fetchAccountInfo(rs);
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
                if (rs.next()) {
                    account = fetchAccountInfo(rs);
                }
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
    
    public boolean deleteAccount(Account account) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate("DELETE FROM `accounts` WHERE `account_no`='" + account.getAccountNumber() + "'");
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while deleting account entry from the database.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close Statement object.\n" + e);
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
        
        return account;
    }
    
    /*==================================================================*
    |                                                                   |
    |                               ADDRESS                             |
    |                                                                   |
    *===================================================================*/
    
    public Address getAddress(String acctNo) {
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
                System.err.println("Error while fetching address from the database.\n" + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close ResultSet and Statement object.");
                }
            }
        }
        return null;
    }
    
    public boolean addAddress(Address address) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(address.generateSQLInsert());
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while adding address entry to database.\n" + e);
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
                    billings.add(fetchBillingInfo(rs));
                }
            } catch (SQLException | ParseException e) {
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
    
    public Billing getBilling(int billingNo) {
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
                System.err.println("Error while fetching billing data.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close Statement and ResultSet objects.\n" + e);
                }
            }
        }
        return billing;
    }
    
    public boolean addBilling(Billing billing) {
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
                System.err.println("Error while adding billing entry to database.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rsKeys != null) rsKeys.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close Statement object. \n" + ex);
                }
            }
        }
        return false;
    }
    
    public boolean deleteBilling(Billing billing) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate("DELETE FROM `billings` WHERE `billing_no`='" + billing.getBillingId() + "'");
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while deleting billing entry from the database.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close Statement object.\n" + e);
                }
            }
        }
        return false;
    }
    
    public boolean updateBilling(int billingNo, Billing updatedBilling) {
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
                        .append("'");
                if (updatedBilling.getDateUpdated() != null) {
                    sb.append(", ")
                            .append("`date_updated`='")
                            .append(Utils.MYSQL_DATETIME_FORMAT.format(updatedBilling.getDateUpdated()))
                            .append("'");
                }
                sb.append(" WHERE (`billing_no`='")
                        .append(billingNo)
                        .append("')");
                int n = s.executeUpdate(sb.toString());
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while updating billing entry.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close statement object.\n" + e);
                }
            }
        }
        return false;
    }
    
    public boolean updateBilling(int billingNo, String columnName, String value) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                String sql = String.format("UPDATE `billings` SET "
                        + "`%s` = '%s' WHERE (`billing_no` = '%d')", columnName, value, billingNo);
                int n = s.executeUpdate(sql);
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while updating billing entry from the database.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close Statement object.\n" + e);
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
    |                               PAYMENTS                            |
    |                                                                   |
    *===================================================================*/
    
    public ArrayList<Payment> getPaymentsByBillingId(String billingId) {
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
                System.err.println("Error while fetching payments with billin_no=" + billingId + ".\n" + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close ResultSet object.\n" + e);
                }
            }
        }
        
        return payments;
    }
    
    public int addPayment(Payment payment) {
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
                System.err.println("Error while adding payment entry to database.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                    if (rsKey != null) rsKey.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close Statement object. \n" + ex);
                }
            }
        }
        return -1;
    }
    
    public boolean deletePayment(Payment payment) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate("DELETE FROM `payments` WHERE `payment_no`='" + payment.getPaymentId()+ "'");
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while deleting payment entry from the database.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close Statement object.\n" + e);
                }
            }
        }
        return false;
    }
    
    public boolean updatePayment(int paymentId, Payment updatedPayment) {
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
                System.err.println("Error while updating payment entry from the database.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close Statement object.\n" + e);
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
                System.err.println("Error while fetching internet subscription entry from database.\n" + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close ResultSet object.\n" + e);
                }
            }
        }
        return null;
    }

    public boolean addInternetSubscription(InternetSubscription internet) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(internet.generateSQLInsert());
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while adding internet entry to database.\n" + e);
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
    |                               PRODUCTS                            |
    |                                                                   |
    *===================================================================*/
    
    public ArrayList<Product> getAllProducts() {
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
                System.err.println("Error while fetching product entries from database.\n" + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close ResultSet object.\n" + e);
                }
            }
        }
        return products;
    }
    
    public boolean addProduct(Product product) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(product.generateSQLInsert());
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while adding product entry to database.\n" + e);
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
    
    public boolean updateProductCount(int productId, int count) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                String sql = String.format("UPDATE `products` SET `count` = '%d' WHERE (`product_no` = '%d')", count, productId);
                int n = s.executeUpdate(sql);
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while updating product count.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close Statement object.\n" + e);
                }
            }
        }
        return false;
    }
    
    public Product findProductByName(String name) {
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
                System.err.println("Error while fetching product (" + name + ").\n" + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close ResultSet and Statement object.\n" + e);
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
                System.err.println("Error while fetching services from database.\n" + e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close ResultSet object.\n" + e);
                }
            }
        }
        return services;
    }
    
    public boolean addService(Service service) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                int n = s.executeUpdate(service.generateSQLInsert());
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while adding service entry to database.\n" + e);
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
    |                               GENERAL                             |
    |                                                                   |
    *===================================================================*/
    
    public boolean update(String keyColumn, String keyValue, String table, String column, String value) {
        if (connection != null && isOpen) {
            Statement s = null;
            try {
                s = connection.createStatement();
                String sql = String.format("UPDATE `%s` SET `%s` = '%s' WHERE (`%s` = '%s')",
                        table, column, value, keyColumn, keyValue);
                int n = s.executeUpdate(sql);
                return n > 0;
            } catch (SQLException e) {
                System.err.println("Error while updating " + table + " entry.\n" + e);
            } finally {
                try {
                    if (s != null) s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close Statement object.\n" + e);
                }
            }
        }
        
        return false;
    }
    
}
