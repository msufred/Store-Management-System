package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingType;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.data.InternetSubscription;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.PaymentListCellFactory;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class EditWISPBillingController extends Controller {

    private Stage stage;
    private Scene scene;
    
    @FXML TextField tfAccount;
    @FXML DatePicker dateFrom;
    @FXML DatePicker dateTo;
    @FXML TextField tfData;
    @FXML TextField tfMonthlyPayment;
    @FXML TextField tfBalance;
    @FXML TextField tfDesc;
    @FXML ListView<Payment> listPayments;
    @FXML TextField tfTotal;
    @FXML Button btnUpdatePrint;
    @FXML Button btnUpdate;
    @FXML Button btnCancel;
    @FXML DatePicker dueDate;
    @FXML TextField tfStatus;
    
    private final BillingsController billingsController;
    private Billing billing;
    
    public EditWISPBillingController(BillingsController billingsController) {
        this.billingsController = billingsController;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listPayments.setCellFactory(new PaymentListCellFactory());
        
        btnUpdatePrint.setOnAction(evt -> {
            ErrorDialog.show("Uh-oh!", "Feature not implemented yet.");
        });
        
        btnUpdate.setOnAction(evt -> {
            if (fieldsValidated()) {
                update();
                close();
                billingsController.refresh();
            }
        });
        
        btnCancel.setOnAction(evt -> {
            if (stage != null) stage.close();
        });
    }

    @Override
    public void onLoadTask() {
        super.onLoadTask(); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void show(int billingNo) {
        clearFields();
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Add Billing");
            if (scene == null) scene = new Scene(getContentPane());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.show();
        loadBilling(billingNo);
    }
    
    public void close() {
        if (stage != null) stage.close();
    }
    
    private void clearFields() {
        tfAccount.clear();
        tfData.clear();
        tfMonthlyPayment.clear();
        tfBalance.clear();
        tfDesc.clear();
        dateFrom.getEditor().clear();
        dateTo.getEditor().clear();
        listPayments.getItems().clear();
        tfTotal.clear();
        dueDate.getEditor().clear();
    }
    
    private void loadBilling(int billingNo) {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                Billing _billing = database.getBilling(billingNo);
                if (_billing != null) {
                    Account account = database.getAccount(_billing.getAccountNo());
                    if (account != null) {
                        _billing.setAccount(account);
                    }
                }
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    showDetails(_billing);
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    ErrorDialog.show(ex.getErrorCode() + "", ex.getLocalizedMessage());
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    private void showDetails(Billing billing) {
        if (billing == null) {
            close();
            ErrorDialog.show("Billing Update Error", "Billing entry is null!");
        } else {
            Account acct = billing.getAccount();
            if (acct != null) {
                tfAccount.setText(acct.toString());
                
                InternetSubscription iSub = acct.getInternetSubscription();
                if (iSub != null) {
                    tfData.setText(iSub.getBandwidth() + " mbps");
                    tfMonthlyPayment.setText("Php " + iSub.getAmount());
                }
                
                tfBalance.setText("0");
                listPayments.setItems(FXCollections.observableArrayList(billing.getPayments()));
                calculateTotal();
                
                dateFrom.setValue(LocalDate.parse(Utils.LOCAL_DATE_FORMAT.format(billing.getFromDate())));
                dateFrom.getEditor().setText(Utils.DATE_FORMAT_2.format(billing.getFromDate()));
                dateTo.setValue(LocalDate.parse(Utils.LOCAL_DATE_FORMAT.format(billing.getToDate())));
                dateTo.getEditor().setText(Utils.DATE_FORMAT_2.format(billing.getToDate()));
                dueDate.getEditor().setText(billing.getDueDate());
                tfStatus.setText(billing.getStatus().toString());
            }
            this.billing = billing;
        }
    }
    
    private void update() {
        ProgressBarDialog.show();
        Billing newBilling = getBillingInfo();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();                
                boolean updated = database.updateBilling(billing.getBillingId(), newBilling);
                if (updated) {
                    History history = new History();
                    history.setDate(Calendar.getInstance().getTime());
                    history.setTitle("Update Billing");
                    history.setDescription(String.format("Updated WISP billing with ID: %d", billing.getBillingId()));
                    database.addHistory(history);
                }
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (!updated) {
                        ErrorDialog.show("Billing Update Error", "Failed to update billing entry.");
                    }
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    ErrorDialog.show(ex.getErrorCode() + "", ex.getLocalizedMessage());
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    private void calculateTotal() {
        if (!listPayments.getItems().isEmpty()) {
            double total = 0;
            for (Payment p : listPayments.getItems()) total += p.getAmount();
            tfTotal.setText(String.valueOf(total));
        }
    }
    
    private Billing getBillingInfo() {
        Billing newBilling = new Billing();
        newBilling.setBillingId(billing.getBillingId());
        newBilling.setAccountNo(billing.getAccountNo());
        newBilling.setAccount(billing.getAccount());
        newBilling.setAmount(Double.parseDouble(tfTotal.getText()));
        newBilling.setBillingDate(billing.getBillingDate()); // we won't change the billing date, instead...
        newBilling.setDateUpdated(Calendar.getInstance().getTime()); // we will set the date updated
        try {
            Date dFrom = Utils.DATE_FORMAT_2.parse(dateFrom.getEditor().getText());
            newBilling.setFromDate(dFrom);
            Date dTo = Utils.DATE_FORMAT_2.parse(dateTo.getEditor().getText());
            newBilling.setToDate(dTo);
        } catch (ParseException e) {
            ErrorDialog.show("Parsing Date Error", e.toString());
        }
        newBilling.setDueDate(dueDate.getEditor().getText());
        newBilling.setStatus(billing.getStatus());
        for (Payment p : listPayments.getItems()) {
            newBilling.addPayment(p);
        }
        newBilling.setType(EnumBillingType.WISP);
        return newBilling;
    }
    
    private boolean fieldsValidated() {
        return !tfTotal.getText().isBlank() &&
                !dueDate.getEditor().getText().isBlank();
    }
}
