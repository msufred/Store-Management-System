package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.AbstractFxmlWindowController;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.data.Service;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class AddWISPController extends AbstractFxmlWindowController {
    
    @FXML ComboBox<Service> cbItems;
    @FXML TextField tfPrice;
    @FXML Spinner<Integer> spQuantity;
    @FXML TextField tfTotal;
    @FXML Button btnCancel;
    @FXML Button btnAdd;
    
    private Billing billing;
    private final BillingsController billingsController;
    private final CompositeDisposable disposables;
    
    public AddWISPController(BillingsController billingsController) {
        super(AddWISPController.class.getResource("add_wisp.fxml"));
        this.billingsController = billingsController;
        disposables = new CompositeDisposable();
    }

    @Override
    public void onFxmlLoaded() {
        Utils.setAsNumericalTextField(tfPrice);
        
        cbItems.valueProperty().addListener((o, s1, s2) -> {
            if (s2 != null) {
                tfPrice.setText(s2.getEstPrice() + "");
            }
        });
        
        tfPrice.textProperty().addListener(o -> calculate());
        
        spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        spQuantity.valueProperty().addListener((ov, v1, v2) -> calculate());
        Utils.setAsIntegerTextField(spQuantity.getEditor());
        
        btnCancel.setOnAction(evt -> closeWindow());
        btnAdd.setOnAction(evt -> {
            if (fieldsValidated()) {
                save();
                closeWindow();
            } else {
                ErrorDialog.show("Please fill in empty fields.", "You might've missed some fields.");
            }
        });
    }

    @Override
    public void onCloseRequest(WindowEvent windowEvent) {
        disposables.dispose();
    }

    public void openWindow(Billing billing) {
        openWindow(); 
        this.billing = billing;
        clearFields();
        loadServices();
    }
    
    private void clearFields() {
        cbItems.getSelectionModel().select(-1);
        tfPrice.clear();
        spQuantity.getValueFactory().setValue(1);
        tfTotal.clear();
    }
    
    private void loadServices() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            ArrayList<Service> services = database.getAllServices();
            Service dataplan = new Service();
            dataplan.setName("Data Plan");
            dataplan.setEstPrice(0);
            services.add(0, dataplan);
            return services;
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(services -> {
                    ProgressBarDialog.close();
                    cbItems.setItems(FXCollections.observableArrayList(services));
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private boolean fieldsValidated() {
        return cbItems.getValue() != null &&
                !tfPrice.getText().isEmpty();
    }
    
    private void calculate() {
        String priceStr = tfPrice.getText().trim();
        if (!priceStr.isEmpty()) {
            double price = Double.parseDouble(tfPrice.getText().trim());
            double total = price * spQuantity.getValue();
            tfTotal.setText(total + "");
        }
    }
    
    private void save() {
        ProgressBarDialog.show();
        Payment payment = getPaymentInfo();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            int id = database.addPayment(payment);

            // if added to the database update billing amount
            if (id > -1) {
                payment.setPaymentId(id);
                billing.addPayment(payment);
                database.updateBilling(billing.getBillingId(), "amount", billing.getAmount() + "");

                // add to history
                History history = new History();
                history.setTitle("Update Billing");
                history.setDescription(String.format("Updated billing with ID %d. Added new item for payment (%s - Php %.2f)",
                        billing.getBillingId(), payment.getName(), payment.getAmount()));
                history.setDate(Utils.getDateNow());
                database.addHistory(history);
            }
            return id;
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(id -> {
                    ProgressBarDialog.close();
                    if (id == -1) {
                        ErrorDialog.show("Database Error", "Failed to add payment entry to the database.");
                    } else {
                        billingsController.updateBillingTable();
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private Payment getPaymentInfo() {
        Payment payment = new Payment();
        Service service = cbItems.getValue();
        if (service != null) {
            payment.setName(service.getName());
        } else {
            payment.setName("< Not Set >");
        }
        payment.setAmount(Double.parseDouble(tfPrice.getText().trim()));
        payment.setQuantity(spQuantity.getValue());
        payment.setTotalAmount(Double.parseDouble(tfTotal.getText().trim()));
        payment.setBillingId(billing.getBillingId());
        return payment;
    }

}
