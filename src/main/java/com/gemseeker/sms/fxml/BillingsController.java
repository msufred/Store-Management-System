package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import com.gemseeker.sms.core.data.EnumBillingType;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.fxml.components.BillingDateTableCellFactory;
import com.gemseeker.sms.fxml.components.BillingDateTableCellFactory2;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.BillingNameTableCellFactory;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 *
 * @author gemini1991
 */
public class BillingsController extends Controller {
    
    @FXML MenuItem miAddWISPBilling;
    @FXML MenuItem miAddItemBilling;
    @FXML Button btnEdit;
    @FXML Button btnDelete;
    @FXML Button btnRefresh;
    @FXML Button btnProcess;
    @FXML TableView<Billing> billingsTable;
    @FXML TableColumn<Billing, Date> colDateOfBilling;
    @FXML TableColumn<Billing, String> colAccountNo;
    @FXML TableColumn<Billing, Account> colName;
    @FXML TableColumn<Billing, String> colDueDate;
    @FXML TableColumn<Billing, Double> colAmount;
    @FXML TableColumn<Billing, String> colStatus;
    @FXML TableColumn<Billing, EnumBillingType> colType;
    
    // details group (billing breakdown)
    @FXML VBox vboxDetails;
    @FXML TableView<Payment> paymentsTable;
    @FXML TableColumn<Payment, String> colDescription;
    @FXML TableColumn<Payment, Double> colCost;
    @FXML TableColumn<Payment, Integer> colQuantity;
    @FXML TableColumn<Payment, Double> colTotal;
    @FXML Button btnAddItem;
    @FXML Button btnEditItem;
    @FXML Button btnDeleteItem;
    @FXML TextField tfTotal;

    private AddWISPBillingController addWISPBillingController;
    private AddItemBillingController addItemBillingController;
    
    private AddItemController addItemController;
    private EditItemController editItemController;
    
    private Billing selectedBilling = null;
    
    @Override
    public void onLoadTask() {
        super.onLoadTask();
        addWISPBillingController = new AddWISPBillingController(this);
        addItemBillingController = new AddItemBillingController(this);
        
        addItemController = new AddItemController(this);
        editItemController = new EditItemController(this);
        
        Loader loader = Loader.getInstance();
        loader.load("fxml/addpayment.fxml", addWISPBillingController);
        loader.load("fxml/add_item_billing.fxml", addItemBillingController);
        loader.load("fxml/add_item.fxml", addItemController);
        loader.load("fxml/edit_item.fxml", editItemController);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDateOfBilling.setCellValueFactory(new PropertyValueFactory<>("billingDate"));
        colDateOfBilling.setCellFactory(new BillingDateTableCellFactory());
        colAccountNo.setCellValueFactory(new PropertyValueFactory<>("accountNo"));
        colName.setCellValueFactory(new PropertyValueFactory<>("account"));
        colName.setCellFactory(new BillingNameTableCellFactory());
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colDueDate.setCellFactory(new BillingDateTableCellFactory2());
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        billingsTable.getSelectionModel().selectedItemProperty().addListener((ov, b1, b2) -> {
            if (b2 != null) {
                showDetails(b2);
                selectedBilling = b2;
            } else {
                clearDetails();
            }
            disableDetailsAndButtons(b2 == null);
        });
        
        // details table & columns
        colDescription.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        paymentsTable.getSelectionModel().selectedItemProperty().addListener((ov, p1, p2) -> {
            disableDetailButtons(p2 == null);
        });
        
        miAddWISPBilling.setOnAction(evt -> {
            if (addWISPBillingController != null) {
                if (!addWISPBillingController.isLoaded()) addWISPBillingController.onLoadTask();
                addWISPBillingController.show();
            }
        });
        
        miAddItemBilling.setOnAction(evt -> {
            if (addItemBillingController != null) {
                if (!addItemBillingController.isLoaded()) addItemBillingController.onLoadTask();
                addItemBillingController.show();
            }
        });
        
        btnDelete.setOnAction(evt -> {
            Billing billing = billingsTable.getSelectionModel().getSelectedItem();
            if (billing != null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, null, ButtonType.YES, ButtonType.NO);
                alert.setTitle("Delete entry?");
                alert.setContentText("You are about to delete this entry. Proceed?");
                Optional<ButtonType> response = alert.showAndWait();
                if (response.isPresent()) {
                    if (response.get() == ButtonType.YES) {
                        deleteBilling(billing);
                    } else {
                        alert.close();
                    }
                }
            }
        });
        
        btnRefresh.setOnAction(evt -> refresh());
        
        // details group
        btnAddItem.setOnAction(evt -> {
            Billing selected = billingsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.getType() == EnumBillingType.WISP) {
                    
                } else {
                    if (addItemController != null) {
                        if (!addItemController.isLoaded()) addItemController.onLoadTask();
                        addItemController.show(selected);
                    }
                }
            }
        });
        
        btnEditItem.setOnAction(evt -> {
            Billing billingSelected = billingsTable.getSelectionModel().getSelectedItem();
            Payment paymentSelected = paymentsTable.getSelectionModel().getSelectedItem();
            if (paymentSelected != null) {
                if (editItemController != null) {
                    if (!editItemController.isLoaded()) editItemController.onLoadTask();
                    editItemController.show(billingSelected, paymentSelected);
                }
            }
        });
        
        btnDeleteItem.setOnAction(evt -> {
            Payment selected = paymentsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, null, ButtonType.YES, ButtonType.NO);
                alert.setTitle("Delete entry?");
                alert.setContentText("You are about to delete this entry. Proceed?");
                Optional<ButtonType> response = alert.showAndWait();
                if (response.isPresent()) {
                    if (response.get() == ButtonType.YES) {
                        deletePayment(selected);
                    } else {
                        alert.close();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume(); 
        refresh();
    }

    public void refresh() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                ArrayList billings = database.getAllBillings();
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    billingsTable.setItems(FXCollections.observableArrayList(billings));
                    selectedBilling = null;
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    ErrorDialog.show(String.valueOf(ex.getErrorCode()), ex.getMessage());
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    /**
     * Doesn't affect the database! Request to display the added Payment entry.
     * Used by the AddItemController only!!!
     * This also avoid the need to refresh the entire table.
     * 
     * @param payment Payment entry from AddItemController
     */
    public void showAddPayment(Payment payment) {
        if (payment != null) {
            paymentsTable.getItems().add(payment);
            calculateTotalAmount();
        }
    }
    
    /**
     * Doesn't affect the database! Request to display updated Payment entry.
     * Used by the EditItemController only!!!
     * 
     * @param payment 
     */
    public void updatePayment(Payment payment) {
        if (payment != null) {
            ObservableList<Payment> items = paymentsTable.getItems();
            for (int i=0; i<items.size(); i++) {
                Payment p = items.get(i);
                if (p.getPaymentId() == payment.getPaymentId()) {
                    items.remove(i);
                    items.add(i, payment);
                    break;
                }
            }
        }
        calculateTotalAmount();
    }
    
    private void deleteBilling(Billing billing) {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            if (billing != null) {
                try {
                    Database database = Database.getInstance();
                    boolean deleted = database.deleteBilling(billing);
                    Platform.runLater(() -> {
                        ProgressBarDialog.close();
                        if (!deleted) {
                            ErrorDialog.show("0x0003", "Failed to delete entry.");
                        }
                        refresh();
                    });
                } catch (SQLException ex) {
                    Platform.runLater(() -> {
                        ProgressBarDialog.close();
                        ErrorDialog.show(String.valueOf(ex.getErrorCode()), ex.getMessage());
                    });
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    private void deletePayment(Payment payment) {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            if (payment != null) {
                try {
                    Database database = Database.getInstance();
                    boolean deleted = database.deletePayment(payment);
                    if (deleted) {
                        Billing billing = billingsTable.getSelectionModel().getSelectedItem();
                        if (billing != null) {
                            billing.getPayments().remove(payment);
                            double newTotal = 0;
                            for (Payment p: billing.getPayments()) {
                                newTotal += p.getTotalAmount();
                            }
                            database.updateBilling(billing.getBillingId(), "amount", newTotal + "");
                        }
                    }
                    Platform.runLater(() -> {
                        ProgressBarDialog.close();
                        if (!deleted) {
                            ErrorDialog.show("0x0003", "Failed to delete entry.");
                        } else {
                            paymentsTable.getItems().remove(payment);
                            calculateTotalAmount();
                        }
                    });
                } catch (SQLException ex) {
                    Platform.runLater(() -> {
                        ProgressBarDialog.close();
                        ErrorDialog.show(String.valueOf(ex.getErrorCode()), ex.getMessage());
                    });
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    private void disableDetailsAndButtons(boolean disable) {
        btnEdit.setDisable(disable);
        btnDelete.setDisable(disable);
        btnProcess.setDisable(disable);
        vboxDetails.setDisable(disable);
    }
    
    private void disableDetailButtons(boolean disable) {
        btnEditItem.setDisable(disable);
        btnDeleteItem.setDisable(disable);
    }
    
    private void showDetails(Billing billing) {
        paymentsTable.setItems(FXCollections.observableArrayList(billing.getPayments()));
        tfTotal.setText(billing.getAmount() + "");
    }
    
    private void clearDetails() {
        paymentsTable.getItems().clear();
    }
    
    private void calculateTotalAmount() {
        if (!paymentsTable.getItems().isEmpty()) {
            double total = 0;
            for (Payment p : paymentsTable.getItems()) {
                total += p.getTotalAmount();
            }
            tfTotal.setText(total + "");
        }
    }
}
