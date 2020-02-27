package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import static com.gemseeker.sms.data.EnumBillingStatus.*;
import com.gemseeker.sms.data.EnumBillingType;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingStatus;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.data.Product;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class AddItemBillingController extends Controller {

    @FXML ComboBox<Account> cbAccounts;
    @FXML HBox addItemGroup;
    @FXML TableView<Payment> itemsTable;
    @FXML TableColumn<Payment, String> colItem;
    @FXML TableColumn<Payment, Double> colPrice;
    @FXML TableColumn<Payment, Integer> colQuantity;
    @FXML TableColumn<Payment, Double> colTotal;
    @FXML ComboBox<Product> cbItems;
    @FXML TextField tfPrice;
    @FXML Spinner spQuantity;
    @FXML TextField tfTotal;
    @FXML DatePicker dueDate;
    @FXML ChoiceBox<EnumBillingStatus> cbStatus;
    @FXML Button btnAdd;
    @FXML Button btnCancel;
    @FXML Button btnSave;
    @FXML Button btnSavePrint;
    
    private Stage stage;
    private Scene scene;
    
    private final BillingsController billingsController;
    
    public AddItemBillingController(BillingsController billingsController) {
        this.billingsController = billingsController;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Utils.setAsNumericalTextField(tfPrice);
        
        cbAccounts.getSelectionModel().selectedItemProperty().addListener((ov, a1, a2) -> {
            disableControls(a2 == null);
            btnSave.setDisable(a2 == null);
            btnSavePrint.setDisable(a2 == null);
        });
        
        colItem.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        cbItems.getSelectionModel().selectedItemProperty().addListener((ov, p1, p2) -> {
            if (p2 != null) {
                tfPrice.setText(p2.getPrice() + "");
            }
        });
        
        spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        
        btnAdd.setOnAction(evt -> {
            if (cbItems.getValue() != null) {
                Payment payment = getPaymentInfo();
                itemsTable.getItems().add(payment);
                calculateTotal();
                // clear fields
                tfPrice.setText("0.0");
                spQuantity.getValueFactory().setValue(1);
            }
        });
        
        cbStatus.setItems(FXCollections.observableArrayList(
                FOR_REVIEW, FOR_DELIVERY, DELIVERED, CANCELLED, FOR_PAYMENT, PAID, OVERDUE
        ));
        cbStatus.getSelectionModel().select(0);
        
        btnCancel.setOnAction(evt -> {
            if (stage != null) stage.close();
        });
        
        btnSave.setOnAction(evt -> {
            if (dueDate.getEditor().getText().isEmpty()) {
                ErrorDialog.show("Oh snap!", "Please set due date.");
            } else {
                save();
                close();
                billingsController.refresh();
            }
        });
        
        btnSavePrint.setOnAction(evt -> {
        
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
        loadData();
    }
    
    public void close() {
        if (stage != null) stage.close();
    }
    
    private void clearFields() {
        itemsTable.getItems().clear();
        tfTotal.clear();
    }
    
    private void loadData() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                ArrayList<Product> products = database.getAllProducts();
                ArrayList<Account> accounts = database.getAllAccounts();
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    cbItems.setItems(FXCollections.observableArrayList(products));
                    cbAccounts.setItems(FXCollections.observableArrayList(accounts));
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
        String name = cbItems.getValue().getName();
        payment.setName(name);
        
        double price = Double.parseDouble(tfPrice.getText().trim());
        payment.setAmount(price);
        
        int qty = Integer.parseInt(spQuantity.getValue().toString());
        payment.setQuantity(qty);
        
        double tAmount = price * qty;
        payment.setTotalAmount(tAmount);
        
        return payment;
    }
    
    private void calculateTotal() {
        if (!itemsTable.getItems().isEmpty()) {
            double total = 0;
            for (Payment p : itemsTable.getItems()) total += p.getAmount();
            tfTotal.setText(total + "");
        }
    }
    
    private void save() {
        ProgressBarDialog.show();
        Billing billing = getBillingInfo();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                boolean added = database.addBilling(billing);
                if (added) {
                    // update inventory
                    for (Payment p : billing.getPayments()) {
                        Product product = database.findProductByName(p.getName());
                        if (product != null) {
                            int newCount = product.getCount() - p.getQuantity();
                            database.updateProductCount(product.getProductId(), newCount);
                        }
                    }
                }
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (!added) {
                        ErrorDialog.show("Database Error", "Failed to add billing entry to the database.");
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
    
    private Billing getBillingInfo() {
        Billing billing = new Billing();
        Account account = cbAccounts.getValue();
        billing.setAccount(account);
        billing.setAccountNo(account.getAccountNumber());
        billing.setBillingDate(Calendar.getInstance().getTime());
//        billing.setAmount(Double.parseDouble(tfTotal.getText()));

        // NOTE: No need to set the total amount here since the Billing already
        // does this. Just add the Payments and it will calculate the total amount
        // itself ;)
        for (Payment p : itemsTable.getItems()) {
            billing.addPayment(p);
        }
        billing.setDueDate(dueDate.getEditor().getText());
        billing.setStatus(cbStatus.getValue());
        billing.setType(EnumBillingType.ITEM);
        
        return billing;
    }
    
    private void disableControls(boolean disable) {
        addItemGroup.setDisable(disable);
        itemsTable.setDisable(disable);
        dueDate.setDisable(disable);
        cbStatus.setDisable(disable);
    }
}
