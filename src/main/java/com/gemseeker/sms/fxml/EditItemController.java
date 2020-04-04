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
import java.util.Calendar;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class EditItemController extends AbstractFxmlWindowController {

    @FXML private TextField tfName;
    @FXML private TextField tfPrice;
    @FXML private TextField tfStock;
    @FXML private TextField tfTotal;
    @FXML private Spinner<Integer> spQuantity;
    @FXML private Button btnCancel;
    @FXML private Button btnUpdate;

    private final BillingsController billingsController;
    private Billing billing; // selected Billing entry
    private Payment payment; // selected Payment entry (belongs to Billing above)
    private Product product;

    private final CompositeDisposable disposables;
    
    public EditItemController(BillingsController billingsController) {
        super(EditItemController.class.getResource("edit_item.fxml"));
        this.billingsController = billingsController;
        disposables = new CompositeDisposable();
    }

    @Override
    public void onFxmlLoaded() {
        spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        spQuantity.valueProperty().addListener((ov, v1, v2) -> {
            if (product != null && payment != null) {
                calculate();
            }
        });

        btnUpdate.setOnAction(evt -> {
            update();
            closeWindow();
        });

        btnCancel.setOnAction(evt -> closeWindow());
    }

    @Override
    public void onCloseRequest(WindowEvent windowEvent) {
        disposables.dispose();
    }
    
    public void openWindow(Billing billing, Payment payment) {
        openWindow(); // AbstractFxmlWindowController
        clearFields();
        this.billing = billing;
        this.payment = payment;
        showDetails();
    }

    private void showDetails() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            return Database.getInstance().findProductByName(payment.getName());
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(p -> {
                    ProgressBarDialog.close();
                    if (p == null) {
                        ErrorDialog.show("Item Update Error", "Failed to get Product info.");
                    } else {
                        tfName.setText(p.getName());
                        tfPrice.setText(p.getPrice() + "");
                        tfStock.setText(p.getCount() + "");

                        if (p.getCount() > 0) {
                            spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, p.getCount()));
                        } else {
                            spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, payment.getQuantity()));
                        }
                        tfTotal.setText(payment.getTotalAmount() + "");
                        spQuantity.getValueFactory().setValue(payment.getQuantity());
                        this.product = p;
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }

    private void calculate() {
        double total = product.getPrice() * spQuantity.getValue();
        tfTotal.setText(total + "");
    }

    private void update() {
        ProgressBarDialog.show();
        Payment updatedPayment = getPaymentInfo();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            boolean updated = database.updatePayment(payment.getPaymentId(), updatedPayment);
            if (updated) {
                // TODO update inventory
                int diff = payment.getQuantity() - updatedPayment.getQuantity();
                int newCount = product.getCount() + diff;
                product.setCount(newCount);
                database.updateProductCount(product.getProductId(), newCount);

                // update billing amount
                // NOTE: Billing.updatePayment() calculates the new total amount
                // so theres no need to manually calculate it
                billing.updatePayment(updatedPayment);
                database.updateBilling(billing.getBillingId(), "amount", billing.getAmount() + "");
                database.updateBilling(billing.getBillingId(), "date_updated",
                        Utils.MYSQL_DATETIME_FORMAT.format(Calendar.getInstance().getTime()));

                // add to history
                History history = new History();
                history.setDate(Calendar.getInstance().getTime());
                history.setTitle("Update Billing");
                history.setDescription(String.format("Updated billing with ID: %d. Updated item: %s", billing.getBillingId(), payment.getName()));
                database.addHistory(history);
            }
            return updated;
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(updated -> {
                    ProgressBarDialog.close();
                    if (!updated) {
                        ErrorDialog.show("Database Error", "Failed to update payment.");
                    } else {
//                        billingsController.updateBillingRow(billing);
//                        billingsController.updatePaymentRow(updatedPayment);
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
        Payment newPayment = new Payment();
        newPayment.setPaymentId(payment.getPaymentId());
        newPayment.setBillingId(payment.getBillingId());
        newPayment.setName(payment.getName());
        newPayment.setDescription(payment.getDescription());
        newPayment.setAmount(payment.getAmount());
        newPayment.setQuantity(spQuantity.getValue());
        newPayment.setTotalAmount(Double.parseDouble(tfTotal.getText().trim()));

        return newPayment;
    }

    private void clearFields() {
        tfName.clear();
        tfPrice.clear();
        tfStock.clear();
        spQuantity.getValueFactory().setValue(1);
        tfTotal.clear();
    }
}
