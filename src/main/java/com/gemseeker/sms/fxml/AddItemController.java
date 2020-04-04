package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.AbstractFxmlWindowController;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.data.Product;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class AddItemController extends AbstractFxmlWindowController {
    
    @FXML private ComboBox<Product> cbItems;
    @FXML private TextField tfPrice;
    @FXML private TextField tfStock;
    @FXML private TextField tfTotal;
    @FXML private Spinner<Integer> spQuantity;
    @FXML private Button btnCancel;
    @FXML private Button btnAdd;
    
    private final BillingsController billingsController;
    private Billing billing;
    
    private int tempItemCount = 0;
    
    private final CompositeDisposable disposables;
    
    public AddItemController(BillingsController billingsController) {
        super(AddItemController.class.getResource("add_item.fxml"));
        this.billingsController = billingsController;
        disposables = new CompositeDisposable();
    }

    @Override
    public void onFxmlLoaded() {
        cbItems.getSelectionModel().selectedItemProperty().addListener((ov, p1, p2) -> {
            if (p2 != null) {
                // show price and in stock
                tfPrice.setText(p2.getPrice() + "");
                tempItemCount = p2.getCount();
                tfStock.setText(tempItemCount + "");
                // calculate total
                calculate();
                // change quantity spinner range values
                spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, tempItemCount));
            }
        });
        
        spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        spQuantity.valueProperty().addListener((ov, v1, v2) -> calculate());
        
        btnAdd.setOnAction(evt -> {
            save();
            closeWindow();
        });
        
        btnCancel.setOnAction(evt -> closeWindow());
    }

    @Override
    public void onCloseRequest(WindowEvent windowEvent) {
        disposables.dispose();
    }

    public void openWindow(Billing billing) {
        super.openWindow();
        clearFields();
        this.billing = billing;
        loadProducts();
    }
    
    private void clearFields() {
        cbItems.getItems().clear();
        tfPrice.clear();
        tfStock.clear();
        tfTotal.clear();
    }
    
    private void loadProducts() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            return Database.getInstance().getAllProducts();
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(products -> {
                    ProgressBarDialog.close();
                    cbItems.setItems(FXCollections.observableArrayList(products));
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private void calculate() {
        double price = cbItems.getSelectionModel().getSelectedItem().getPrice();
        double total = price * spQuantity.getValue();
        tfTotal.setText(total + "");
    }
    
    private void save() {
        ProgressBarDialog.show();
        Payment payment = getPaymentInfo();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            int id = database.addPayment(payment);

            // if added to the database, update the inventory count
            // and billing amount
            if (id > -1) {
                // update inventory count
                Product product = cbItems.getValue();
                int newCount = product.getCount() - payment.getQuantity();
                database.updateProductCount(product.getProductId(), newCount);

                // update billing total amount
                // NOTE: Adding Payment to Billing automatically calculates the
                // Billing's total amount.
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
                        //billingsController.updateBillingRow(billing);
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
        Product product = cbItems.getValue();
        String name = product.getName();
        payment.setName(name);
        payment.setAmount(product.getPrice());
        payment.setQuantity(spQuantity.getValue());
        payment.setTotalAmount(Double.parseDouble(tfTotal.getText().trim()));
        payment.setBillingId(billing.getBillingId());
        return payment;
    }
}
