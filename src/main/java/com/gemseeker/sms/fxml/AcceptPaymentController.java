package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Balance;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.BillingProcessed;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingStatus;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.InfoDialog;
import com.gemseeker.sms.fxml.components.PaymentListCellFactory;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import com.gemseeker.sms.fxml.components.QuestionDialog;
import java.net.URL;
import java.sql.SQLException;
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
    @FXML private TextField tfAmountReceived;
    @FXML private TextField tfChange;
    @FXML private TextArea taRemarks;
    
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    
    private Stage stage;
    private Scene scene;
    
    private final BillingsController billingsController;
    private Billing billing;
    
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
            confirmAndPrint();
        });
    }

    @Override
    public void onLoadTask() {
        super.onLoadTask(); //To change body of generated methods, choose Tools | Templates.
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
        InfoDialog.show("Not Implemented Yet", "This feature is currently in development.");
    }
    
    private void showDetails(Billing billing) {
        lblBillingNo.setText(billing.getBillingId() + "");
        lblAccountNo.setText(billing.getAccountNo());
        String name = String.format("%s %s",
                billing.getAccount().getFirstName(),
                billing.getAccount().getLastName());
        lblAccountName.setText(name);

        items.setItems(FXCollections.observableArrayList(billing.getPayments()));
        
        tfAmountDue.setText(billing.getAmount() + "");
    }
    
    private void calculate() {
        try {
            String receivedStr = tfAmountReceived.getText();
            if (receivedStr.isEmpty()) {
                tfChange.setText("0");
            } else {
                double amountDue = billing.getAmount();
                double received = Double.parseDouble(receivedStr.trim());
                double change = received - amountDue;
                tfChange.setText(change + "");
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
    }
}
