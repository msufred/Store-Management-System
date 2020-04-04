package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.AbstractFxmlWindowController;
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
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class EditWISPBillingController extends AbstractFxmlWindowController {

    @FXML private TextField tfAccount;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TextField tfData;
    @FXML private TextField tfMonthlyPayment;
    @FXML private TextField tfBalance;
    @FXML private TextField tfDesc;
    @FXML private ListView<Payment> listPayments;
    @FXML private TextField tfTotal;
    @FXML private Button btnUpdatePrint;
    @FXML private Button btnUpdate;
    @FXML private Button btnCancel;
    @FXML private DatePicker dueDate;
    @FXML private TextField tfStatus;
    
    private final BillingsController billingsController;
    private final CompositeDisposable disposables;
    private Billing billing;
    
    public EditWISPBillingController(BillingsController billingsController) {
        super(EditWISPBillingController.class.getResource("edit_wisp_billing.fxml"));
        this.billingsController = billingsController;
        disposables = new CompositeDisposable();
    }
    
    @Override
    public void onFxmlLoaded() {
        listPayments.setCellFactory(new PaymentListCellFactory());
        
        btnUpdatePrint.setOnAction(evt -> {
            ErrorDialog.show("Uh-oh!", "Feature not implemented yet.");
        });
        
        btnUpdate.setOnAction(evt -> {
            if (fieldsValidated()) {
                update();
                closeWindow();
                billingsController.refresh();
            }
        });
        
        btnCancel.setOnAction(evt -> closeWindow());
    }

    @Override
    public void onCloseRequest(WindowEvent windowEvent) {
        disposables.dispose();
    }
    
    public void openWindow(int billingNo) {
        openWindow(); // AbstractFxmlWindowController
        clearFields();
        loadBilling(billingNo);
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
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            Billing _billing = database.getBilling(billingNo);
            if (_billing != null) {
                Account account = database.getAccount(_billing.getAccountNo());
                if (account != null) {
                    _billing.setAccount(account);
                }
            }
            return _billing;
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(b -> {
                    ProgressBarDialog.close();
                    showDetails(b);
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private void showDetails(Billing billing) {
        if (billing == null) {
            closeWindow();
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
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();                
            boolean updated = database.updateBilling(billing.getBillingId(), newBilling);
            if (updated) {
                History history = new History();
                history.setDate(Calendar.getInstance().getTime());
                history.setTitle("Update Billing");
                history.setDescription(String.format("Updated WISP billing with ID: %d", billing.getBillingId()));
                database.addHistory(history);
            }
            return updated;
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(updated -> {
                    ProgressBarDialog.close();
                    if (!updated) {
                        ErrorDialog.show("Billing Update Error", "Failed to update billing entry.");
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
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
