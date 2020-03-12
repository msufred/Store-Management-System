package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Balance;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.BillingProcessed;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingStatus;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.data.Revenue;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.PaymentListCellFactory;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import com.gemseeker.sms.fxml.components.QuestionDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class AcceptPaymentController extends Controller {
    
    @FXML private Label lblBillingNo;
    @FXML private Label lblAccountNo;
    @FXML private Label lblAccountName;
    @FXML private ListView<Payment> items;
    
    @FXML private TextField tfAmountDue;
    @FXML private TextField tfBalance;
    @FXML private TextField tfAmountReceived;
    @FXML private TextField tfTotal;
    @FXML private TextField tfChange;
    @FXML private TextArea taRemarks;
    
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    
    private Stage stage;
    private Scene scene;
    
    private final BillingsController billingsController;
    private PrintReceiptController printReceiptController;
    private Billing billing;
    
    private double mAmountDue;
    private double mBalance;
    private double mChange;
    
    public AcceptPaymentController(BillingsController billingsController) {
        this.billingsController = billingsController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Utils.setAsNumericalTextFields(tfAmountDue, tfAmountReceived, tfChange);
        tfAmountReceived.textProperty().addListener((o, t1, t2) -> calculate());
        items.setCellFactory(new PaymentListCellFactory());
        btnCancel.setOnAction(evt -> close());
        btnConfirm.setOnAction(evt -> {
            String receivedStr = tfAmountReceived.getText();
            if (receivedStr.isEmpty()) {
                ErrorDialog.show("No Payment Received", "Please enter amount.");
            } else {
                double d = Double.parseDouble(receivedStr);
                if (d == 0) {
                    ErrorDialog.show("No Payment Received", "Please enter amount.");
                } else {
                    confirmAndPrint();
                }
            }
        });
    }

    @Override
    public void onLoadTask() {
        super.onLoadTask();
        printReceiptController = new PrintReceiptController();
        Loader loader = Loader.getInstance();
        loader.load("fxml/receipt.fxml", printReceiptController);
    }

    public void show(Billing billing) {
        if (billing == null) {
            ErrorDialog.show("Uh-oh!", "Selected billing entry is null!");
            return;
        }
        clearFields();
        if (stage == null) {
            stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Accept Payment");
            scene = new Scene(getContentPane());
            stage.setScene(scene);
        }
        stage.show();
        showDetails(billing);
        this.billing = billing;
    }
    
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }
    
    private void confirmAndPrint() {
        String amountStr = tfAmountReceived.getText();
        String changeStr = tfChange.getText();
        String remarks = taRemarks.getText();
        
        if (amountStr.isEmpty()) {
            ErrorDialog.show("No Amount Received", "Please type amount received.");
        } else {
            ProgressBarDialog.show();
            Thread t = new Thread(() -> {
                try {
                    Database database = Database.getInstance();
                    BillingProcessed bp = new BillingProcessed();
                    bp.setBillingNo(billing.getBillingId());
                    bp.setAmountDue(billing.getAmount());
                    bp.setAmountPaid(Double.parseDouble(amountStr));
                    bp.setDateOfTransaction(Calendar.getInstance().getTime());
                    bp.setRemarks(remarks);
                    
                    int key = database.addBillingProcessed(bp);
                    
                    if (key > -1) {
                        // change billing status
                        database.updateBilling(billing.getBillingId(), "status", EnumBillingStatus.PAID.getName());
                        database.updateBilling(billing.getBillingId(), "date_updated",
                                Utils.MYSQL_DATETIME_FORMAT.format(Calendar.getInstance().getTime()));
                        
                        // set all balance to paid
                        Account acct = billing.getAccount();
                        if (acct != null) {
                            ArrayList<Balance> balances = acct.getBalances();
                            if (!balances.isEmpty()) {
                                for (Balance b : balances) {
                                    if (!b.isIsPaid()) {
                                        database.updateBalance(b.getBalanceNo(), true);
                                    }
                                }
                            }
                        }
                        
                        // add balance entry if possible
                        if (changeStr != null && !changeStr.isEmpty()) {
                            double change = Double.parseDouble(changeStr.trim());
                            
                            // if change is less than 0, add as balance
                            if (change < 0) {
                                Balance balance = new Balance();
                                balance.setBillingProcessedNo(key);
                                balance.setAccountNo(billing.getAccountNo());
                                balance.setAmount(Math.abs(change));

                                database.addBalance(balance);
                            }
                        }
                        
                        // add to revenues
                        Revenue revenue = new Revenue();
                        revenue.setAmount(Double.parseDouble(amountStr));
                        revenue.setType(billing.getType().getName());
                        if (acct != null) {
                            revenue.setDescription(String.format("Paid by %s %s on %s",
                                    acct.getFirstName(), acct.getLastName(), Utils.DATE_FORMAT_1.format(Utils.getDateNow())));
                        }
                        revenue.setDate(Utils.getDateNow());
                        boolean addedToIncome = database.addRevenue(revenue);
                        if (!addedToIncome) {
                            System.err.println("Failed to add income entry!");
                        }
                        
                        // add history
                        History history = new History();
                        history.setDate(Calendar.getInstance().getTime());
                        history.setTitle("Accept Payment");
                        history.setDescription(String.format("Accepted payment for billing [ID: %d] the amount of Php %.2f (due amount: Php %.2f)",
                                billing.getBillingId(), bp.getAmountPaid(), bp.getAmountDue()));
                        database.addHistory(history);
                    }
                    
                    Platform.runLater(() -> {
                        ProgressBarDialog.close();
                        if (key == -1) {
                            ErrorDialog.show("Oh Snap!", "Failed to process billing!");
                        }
                        
                        QuestionDialog.show("Print Receipt?", "Click YES to print receipt or NO to close this "
                                + "window.", 
                                () -> {
                                    printReceipt();
                                }, "YES", "NO");
                        
                        close();
                        billingsController.updateBillingTable();
                    });
                } catch (SQLException ex) {
                    Platform.runLater(() -> {
                        ProgressBarDialog.close();
                        ErrorDialog.show(ex.getErrorCode() + "", ex.getLocalizedMessage());
                        close();
                    });
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }
    
    private void printReceipt() {
        if (printReceiptController != null && billing != null) {
            if (!printReceiptController.isLoaded()) printReceiptController.onLoadTask();
            printReceiptController.show(billing);
        }
    }
    
    private void showDetails(Billing billing) {
        lblBillingNo.setText(billing.getBillingId() + "");
        lblAccountNo.setText(billing.getAccountNo());
        String name = String.format("%s %s",
                billing.getAccount().getFirstName(),
                billing.getAccount().getLastName());
        lblAccountName.setText(name);

        items.setItems(FXCollections.observableArrayList(billing.getPayments()));
        
        Account acct = billing.getAccount();
        double balance = 0;
        if (acct != null) {
            ArrayList<Balance> balances = acct.getBalances();
            if (!balances.isEmpty()) {
                for (Balance b : balances) {
                    if (!b.isIsPaid()) {
                        balance += b.getAmount();
                    }
                }
            }
        }
        mAmountDue = billing.getAmount();
        mBalance = balance;
        
        tfAmountDue.setText(mAmountDue + "");
        tfBalance.setText(mBalance + "");
        tfTotal.setText(String.valueOf(mAmountDue + mBalance));
    }
    
    private void calculate() {
        try {
            String receivedStr = tfAmountReceived.getText();
            if (receivedStr.isEmpty()) {
                tfChange.setText("0");
            } else {
                double received = Double.parseDouble(receivedStr.trim());
                mChange = received - (mAmountDue + mBalance);
                tfChange.setText(mChange + "");
            }
        } catch (NumberFormatException ex) {
            ErrorDialog.show("Oh snap!", "Failed to calculate! :P");
        }
    }
    
    private void clearFields() {
        lblBillingNo.setText("< Not Set >");
        lblAccountNo.setText("< Not Set >");
        lblAccountName.setText("< Not Set >");
        items.getItems().clear();
        
        tfAmountDue.setText("0.0");
        tfAmountReceived.setText("0.0");
        tfChange.setText("0.0");
        
        mAmountDue = 0;
        mBalance = 0;
        mChange = 0;
    }
}
