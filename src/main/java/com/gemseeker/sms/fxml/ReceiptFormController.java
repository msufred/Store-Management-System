package com.gemseeker.sms.fxml;



import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Balance;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.BillingProcessed;
import com.gemseeker.sms.data.Payment;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author gemini1991
 */
public class ReceiptFormController extends Controller {
    
    @FXML private Label lblDate;
    @FXML private Label lblAccountNo;
    @FXML private Label lblName;
    @FXML private Label lblAddress;
    @FXML private Label lblDueDate;
    @FXML private Label lblBalance;
    @FXML private Label lblDueBalance;
    @FXML private Label lblAmountPaid;
    @FXML private Label lblAmountDue;
    @FXML private TableView<Payment> itemsTable;
    @FXML private TableColumn<Payment, String> colName;
    @FXML private TableColumn<Payment, Double> colPrice;
    @FXML private TableColumn<Payment, Integer> colQty;
    @FXML private TableColumn<Payment, Double> colTotal;
    
    // copy
    @FXML private Label lblDate1;
    @FXML private Label lblAccountNo1;
    @FXML private Label lblName1;
    @FXML private Label lblAddress1;
    @FXML private Label lblDueDate1;
    @FXML private Label lblBalance1;
    @FXML private Label lblDueBalance1;
    @FXML private Label lblAmountPaid1;
    @FXML private Label lblAmountDue1;
    @FXML private TableView<Payment> itemsTable1;
    @FXML private TableColumn<Payment, String> colName1;
    @FXML private TableColumn<Payment, Double> colPrice1;
    @FXML private TableColumn<Payment, Integer> colQty1;
    @FXML private TableColumn<Payment, Double> colTotal1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itemsTable.setSelectionModel(null);
        itemsTable.setSelectionModel(null);
        
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        colName1.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice1.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colQty1.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal1.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    }
    
    public void setBillingProcessed(BillingProcessed billingProcessed) {
        if (billingProcessed != null) {
            showDetails(billingProcessed);
        }
    }
    
    private void showDetails(BillingProcessed billingProcessed) {
        Billing billing = billingProcessed.getBilling();
        Account account = billing.getAccount();
        if (account != null) {
            lblAccountNo.setText(account.getAccountNumber());
            lblName.setText(String.format("%s %s", account.getFirstName(), account.getLastName()));
            lblAddress.setText(account.getAddress().toString());
            
            lblAccountNo1.setText(account.getAccountNumber());
            lblName1.setText(String.format("%s %s", account.getFirstName(), account.getLastName()));
            lblAddress1.setText(account.getAddress().toString());
            
            ArrayList<Balance> balances = account.getBalances();
            if (!balances.isEmpty()) {
                for (Balance b : balances) {
                    double tBalance = 0;
                    if (!b.isIsPaid()) {
                        tBalance += b.getAmount();
                    }
                    lblBalance.setText(tBalance + "");
                    lblBalance1.setText(tBalance + "");
                }
            }
        }
        
        lblDueDate.setText(billing.getDueDate());
        lblDueDate1.setText(billing.getDueDate());
        
        itemsTable.setItems(FXCollections.observableArrayList(billing.getPayments()));
        itemsTable1.setItems(FXCollections.observableArrayList(billing.getPayments()));
        
        double amountDue = billingProcessed.getAmountDue();
        double amountPaid = billingProcessed.getAmountPaid();
        
        lblAmountDue.setText(amountDue + "");
        lblAmountDue1.setText(amountDue + "");
        
        lblAmountPaid.setText(amountPaid + "");
        lblAmountPaid1.setText(amountPaid + "");
        
        lblDueBalance.setText(String.valueOf(Math.abs(amountDue - amountPaid)));
        lblDueBalance1.setText(String.valueOf(Math.abs(amountDue - amountPaid)));
        
        lblDate.setText(Utils.DATE_FORMAT_1.format(Calendar.getInstance().getTime()));
        lblDate1.setText(Utils.DATE_FORMAT_1.format(Calendar.getInstance().getTime()));
    }
    
    public void clear() {
        lblAccountNo.setText("");
        lblName.setText("");
        lblAddress.setText("");
        lblBalance.setText("0.0");
        lblAmountDue.setText("0.0");
        lblAmountPaid.setText("0.0");
        lblDueBalance.setText("0.0");
        itemsTable.getItems().clear();
        
        lblAccountNo1.setText("");
        lblName1.setText("");
        lblAddress1.setText("");
        lblBalance1.setText("0.0");
        lblAmountDue1.setText("0.0");
        lblAmountPaid1.setText("0.0");
        lblDueBalance1.setText("0.0");
        itemsTable1.getItems().clear();
    }
}
