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

/**
 *
 * @author gemini1991
 */
public class AddItemController extends Controller {
    
    @FXML ComboBox<Product> cbItems;
    @FXML TextField tfPrice;
    @FXML TextField tfStock;
    @FXML TextField tfTotal;
    @FXML Spinner<Integer> spQuantity;
    @FXML Button btnCancel;
    @FXML Button btnAdd;
    
    private Stage stage;
    private Scene scene;
    
    private final BillingsController billingsController;
    private Billing billing;
    
    private int tempItemCount = 0;
    
    public AddItemController(BillingsController billingsController) {
        this.billingsController = billingsController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
            close();
        });
        
        btnCancel.setOnAction(evt -> close());
    }

    @Override
    public void onLoadTask() {
        super.onLoadTask(); 
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void show(Billing billing) {
        if (billing == null) {
            ErrorDialog.show("0x0006", "No selected billing entry!");
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
        loadProducts();
    }
    
    public void close() {
        if(stage != null) stage.close();
    }
    
    private void loadProducts() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                ArrayList<Product> products = database.getAllProducts();
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    cbItems.setItems(FXCollections.observableArrayList(products));
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
    
    private void calculate() {
        double price = cbItems.getSelectionModel().getSelectedItem().getPrice();
        double total = price * spQuantity.getValue();
        tfTotal.setText(total + "");
    }
    
    private void save() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                Payment payment = getPaymentInfo();
                boolean added = database.addPayment(payment);
                
                if (added) {
                    // update inventory count
                    Product product = cbItems.getValue();
                    int newCount = product.getCount() - payment.getQuantity();
                    database.updateProductCount(product.getProductId(), newCount);
                    
                    // update billing total amount
                    billing.addPayment(payment);
                    double newTotal = 0;
                    for (Payment p : billing.getPayments()) {
                        newTotal += p.getTotalAmount();
                    }
                    database.updateBilling(billing.getBillingId(), "amount", newTotal + "");
                }
                
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (!added) {
                        ErrorDialog.show("0x0002", "Failed to add payment entry to the database.");
                    } else {
                        billingsController.showAddPayment(payment);
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
