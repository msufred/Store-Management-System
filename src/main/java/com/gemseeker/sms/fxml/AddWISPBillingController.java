package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import static com.gemseeker.sms.data.EnumBillingStatus.*;
import com.gemseeker.sms.data.EnumBillingType;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingStatus;
import com.gemseeker.sms.data.InternetSubscription;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.data.Service;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.PaymentListCellFactory;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class AddWISPBillingController extends Controller {

    private Stage stage;
    private Scene scene;
    
    @FXML ChoiceBox<Account> cbAccounts;
    @FXML DatePicker dateFrom;
    @FXML DatePicker dateTo;
    @FXML ComboBox<Service> cbPaymentType;
    @FXML TextField tfData;
    @FXML TextField tfMonthlyPayment;
    @FXML TextField tfBalance;
    @FXML TextField tfDesc;
    @FXML TextField tfAmount;
    @FXML Spinner<Integer> spQuantity;
    @FXML Button btnAdd;
    @FXML ListView<Payment> listPayments;
    @FXML TextField tfTotal;
    @FXML Button btnSavePrint;
    @FXML Button btnSaveOnly;
    @FXML Button btnCancel;
    @FXML DatePicker dueDate;
    @FXML ChoiceBox<EnumBillingStatus> cbStatus;
    
    private final BillingsController billingsController;
    
    public AddWISPBillingController(BillingsController billingsController) {
        this.billingsController = billingsController;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // set numerical textfields
        Utils.setAsNumericalTextFields(tfAmount);
        
        spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        
        cbAccounts.valueProperty().addListener((ov, t, t1) -> {
            if (t1 != null) {
                Platform.runLater(() -> {
                    btnSavePrint.setDisable(false);
                    btnSaveOnly.setDisable(false);
                    showDetails(t1);
                });
            }
        });
        
        cbPaymentType.valueProperty().addListener((ov, s1, s2) -> {
            if (s2 != null) {
                tfAmount.setText(s2.getEstPrice() + "");
            }
        });
        
        listPayments.setCellFactory(new PaymentListCellFactory());
        
        cbStatus.setItems(FXCollections.observableArrayList(
                FOR_REVIEW, FOR_PAYMENT, PAID, OVERDUE
        ));
        cbStatus.getSelectionModel().select(0);
        
        btnAdd.setOnAction(evt -> {
            // if amount and quantity is not empty or blank, add to list, then calculate total
            if (!tfAmount.getText().isEmpty()) {
                Payment payment = getPaymentInfo();
                listPayments.getItems().add(payment);
                calculateTotal();
                
                // clear fields
                cbPaymentType.getSelectionModel().select(-1);
                tfAmount.clear();
                spQuantity.getValueFactory().setValue(1);
            }
        });
        
        btnSavePrint.setOnAction(evt -> {
            ErrorDialog.show("Uh-oh!", "Feature not implemented yet.");
        });
        
        btnSaveOnly.setOnAction(evt -> {
            if (fieldsValidated()) {
                save();
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
        super.onLoadTask(); 
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void show() {
        clearFields();
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Add Billing");
            if (scene == null) scene = new Scene(getContentPane());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.show();
        loadAccounts();
        loadServices();
    }
    
    public void close() {
        if (stage != null) stage.close();
    }
    
    private void loadServices() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                ArrayList<Service> services = database.getAllServices();
                Service dataplan = new Service();
                dataplan.setName("Data Plan");
                dataplan.setEstPrice(0);
                services.add(0, dataplan);
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    cbPaymentType.setItems(FXCollections.observableArrayList(services));
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
    
    private void clearFields() {
        cbAccounts.getSelectionModel().select(-1);
        tfData.clear();
        tfMonthlyPayment.clear();
        tfBalance.clear();
        tfDesc.clear();
        dateFrom.getEditor().clear();
        dateTo.getEditor().clear();
        cbPaymentType.getSelectionModel().select(-1);
        tfAmount.clear();
        spQuantity.getValueFactory().setValue(1);
        listPayments.getItems().clear();
        tfTotal.clear();
        dueDate.getEditor().clear();
    }
    
    private void showDetails(Account account) {
        InternetSubscription isub = account.getInternetSubscription();
        if(isub != null) {
            tfData.setText(isub.getBandwidth() + " mbps");
            tfMonthlyPayment.setText("Php " + isub.getAmount());
        }
    }
    
    private void loadAccounts() {
        try {
            Database database = Database.getInstance();
            ArrayList<Account> accts = database.getAllAccounts();
            cbAccounts.setItems(FXCollections.observableArrayList(accts));
        } catch (SQLException ex) {
            Logger.getLogger(AddWISPBillingController.class.getName()).log(Level.SEVERE, null, ex);
            if (stage != null) stage.close();
        }
    }
    
    private Payment getPaymentInfo() {
        Payment payment = new Payment();
        
        Service service = cbPaymentType.getValue();
        if (service != null) {
            payment.setName(service.getName());
        } else {
            payment.setName("< Not Set >");
        }
        
        String amountStr = tfAmount.getText();
        double amount = Double.parseDouble(amountStr);
        payment.setAmount(amount);
        
        int qty = spQuantity.getValueFactory().getValue();
        payment.setQuantity(qty);
        
        
        double tAmount = amount;
        if (qty > 0) {
            tAmount = amount * qty; // price x quantity
        }
        payment.setTotalAmount(tAmount);
        
        return payment;
    }
    
    private void calculateTotal() {
        if (!listPayments.getItems().isEmpty()) {
            double total = 0;
            for (Payment p : listPayments.getItems()) total += p.getAmount();
            tfTotal.setText(String.valueOf(total));
        }
    }
    
    private void save() {
        ProgressBarDialog.show();
        Billing billing = getBillingInfo();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                boolean added = database.addBilling(billing);
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (!added) {
                        ErrorDialog.show("Database Error", "Failed to add new billing entry.");
                    }
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    ErrorDialog.show(ex.getErrorCode() + "", ex.getMessage());
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    private Billing getBillingInfo() {
        Billing billing = new Billing();
        
        Account acct = cbAccounts.getValue();
        if (acct == null) return null;
        
        billing.setAccountNo(acct.getAccountNumber());
//        billing.setAmount(Double.parseDouble(tfTotal.getText()));
        billing.setBillingDate(Calendar.getInstance(Locale.getDefault()).getTime());
        try {
            Date dFrom = Utils.DATE_FORMAT_2.parse(dateFrom.getEditor().getText());
            billing.setFromDate(dFrom);
            Date dTo = Utils.DATE_FORMAT_2.parse(dateTo.getEditor().getText());
            billing.setToDate(dTo);
        } catch (ParseException e) {
            ErrorDialog.show("Date Parsing Error", e.toString());
        }
        billing.setDueDate(dueDate.getEditor().getText());
        billing.setStatus(cbStatus.getValue());
        
        // NOTE: No need to set the total amount here since the Billing object
        // will automatically (in a sense) calculate it. Just add the Payments
        // and the total amount is calculated. :)
        for (Payment p : listPayments.getItems()) {
            billing.addPayment(p);
        }
        
        billing.setType(EnumBillingType.WISP);
        
        return billing;
    }
    
    private boolean fieldsValidated() {
        return cbAccounts.getValue() != null &&
                !tfTotal.getText().isBlank() &&
                !dueDate.getEditor().getText().isBlank();
    }
}
