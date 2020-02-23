package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.data.Product;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class EditItemController extends Controller {
    
    @FXML TextField tfName;
    @FXML TextField tfPrice;
    @FXML TextField tfStock;
    @FXML TextField tfTotal;
    @FXML Spinner<Integer> spQuantity;
    @FXML Button btnCancel;
    @FXML Button btnUpdate;
    
    private Stage stage;
    private Scene scene;
    
    private final BillingsController billingsController;
    private Billing billing; // selected Billing entry
    private Payment payment; // selected Payment entry (belongs to Billing above)
    private Product product;
    
    public EditItemController(BillingsController billingsController) {
        this.billingsController = billingsController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        spQuantity.valueProperty().addListener((ov, v1, v2) -> {
            if (product != null && payment != null) {
                calculate();
            }
        });
        
        btnUpdate.setOnAction(evt -> {
            update();
            close();
        });
        
        btnCancel.setOnAction(evt -> close());
    }

    @Override
    public void onLoadTask() {
        super.onLoadTask(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onResume() {
        super.onResume(); //To change body of generated methods, choose Tools | Templates.
    }

    public void show(Billing billing, Payment payment) {
        // making sure Billing and Payment entries are not null!
        if (billing == null || payment == null) {
            ErrorDialog.show("0x0006", "No Billing and Payment entry selected.");
            return;
        }
        
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Add Item");
            stage.initModality(Modality.APPLICATION_MODAL);
            
            scene = new Scene(getContentPane());
            stage.setScene(scene);
            stage.setAlwaysOnTop(true);
        }
        stage.show();
        this.billing = billing;
        this.payment = payment;
        showDetails();
    }
    
    public void close() {
        if(stage != null) stage.close();
    }

    private void showDetails() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                Product p = database.findProductByName(payment.getName());
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (p == null) ErrorDialog.show("0x0005", "Failed to get Product info.");
                    else {
                        showProductDetails(p);
                        tfTotal.setText(payment.getTotalAmount() + "");
                        spQuantity.getValueFactory().setValue(payment.getQuantity());
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
    
    private void showProductDetails(Product product) {
        tfName.setText(product.getName());
        tfPrice.setText(product.getPrice() + "");
        tfStock.setText(product.getCount() + "");
        
        spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, product.getCount()));
        this.product = product;
    }
    
    private void calculate() {
        double total = product.getPrice() * spQuantity.getValue();
        tfTotal.setText(total + "");
    }
    
    private void update() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                Payment updatedPayment = getPaymentInfo();
                boolean updated = database.updatePayment(payment.getPaymentId(), updatedPayment);
                
                if (updated) {
                    // TODO update inventory
                    int diff = payment.getQuantity() - updatedPayment.getQuantity();
                    int newCount = product.getCount() + diff;
                    product.setCount(newCount);
                    database.updateProductCount(product.getProductId(), newCount);
                    
                    // TODO update billing total amount
                    for (int i=0; i<billing.getPayments().size(); i++) {
                        Payment p = billing.getPayments().get(i);
                        if (p.getPaymentId() == updatedPayment.getPaymentId()) {
                            billing.getPayments().remove(payment);
                            billing.getPayments().add(i, updatedPayment);
                            break;
                        }
                    }
                    
                    double newTotal = 0;
                    for (Payment p : billing.getPayments()) {
                        newTotal += p.getTotalAmount();
                    }
                    
                    billing.setAmount(newTotal);
                    database.updateBilling(billing.getBillingId(), "amount", newTotal + "");
                }
                
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (!updated) {
                        ErrorDialog.show("0x0005", "Failed to update payment.");
                    } else {
                        billingsController.updatePayment(updatedPayment);
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
}
