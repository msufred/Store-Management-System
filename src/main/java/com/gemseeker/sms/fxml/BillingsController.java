package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import static com.gemseeker.sms.data.EnumBillingStatus.*;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingStatus;
import com.gemseeker.sms.data.EnumBillingType;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.data.Product;
import com.gemseeker.sms.fxml.components.BillingDateTableCellFactory;
import com.gemseeker.sms.fxml.components.BillingDateTableCellFactory2;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.BillingNameTableCellFactory;
import com.gemseeker.sms.fxml.components.BillingTableRow;
import com.gemseeker.sms.fxml.components.InfoDialog;
import com.gemseeker.sms.fxml.components.QuestionDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
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
    @FXML MenuButton mbWISP;
    @FXML MenuItem miForPaymentWISP;
    @FXML MenuItem miReceivePaymentWISP;
    @FXML MenuItem miExtendDueDateWISP;
    @FXML MenuButton mbItems;
    @FXML MenuItem miReceivePaymentItem;
    @FXML MenuItem miExtendDueDateItem;
    @FXML MenuItem miDeliverOrder;
    @FXML MenuItem miCancelOrder;
    @FXML ComboBox<String> cbFilterType;
    @FXML ComboBox<String> cbFilterStatus;
    
    @FXML TableView<Billing> billingsTable;
    @FXML TableColumn<Billing, Date> colDateOfBilling;
    @FXML TableColumn<Billing, String> colAccountNo;
    @FXML TableColumn<Billing, Account> colName;
    @FXML TableColumn<Billing, String> colDueDate;
    @FXML TableColumn<Billing, Double> colAmount;
    @FXML TableColumn<Billing, String> colStatus;
    @FXML TableColumn<Billing, EnumBillingType> colType;
    
    // details group (billing breakdown)
    @FXML TitledPane detailsGroup;
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
    private EditWISPBillingController editWISPBillingController;
    private AddItemBillingController addItemBillingController;
    
    private AddItemController addItemController;
    private EditItemController editItemController;
    private AddWispController addWispController;
    
    private int mCurrentBillingIndex = -1; // current selected Billing entry in table
    private int mLastBillingIndex = -1; // holds the last index selected (can be used after refresh)
    
    @Override
    public void onLoadTask() {
        super.onLoadTask();
        addWISPBillingController = new AddWISPBillingController(this);
        editWISPBillingController = new EditWISPBillingController(this);
        addItemBillingController = new AddItemBillingController(this);
        
        addItemController = new AddItemController(this);
        editItemController = new EditItemController(this);
        addWispController = new AddWispController(this);
        
        Loader loader = Loader.getInstance();
        loader.load("fxml/add_wisp_billing.fxml", addWISPBillingController);
        loader.load("fxml/edit_wisp_billing.fxml", editWISPBillingController);
        loader.load("fxml/add_item_billing.fxml", addItemBillingController);
        loader.load("fxml/add_item.fxml", addItemController);
        loader.load("fxml/edit_item.fxml", editItemController);
        loader.load("fxml/add_wisp.fxml", addWispController);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // billings table and columns
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
        
        billingsTable.setRowFactory(row -> new BillingTableRow());
        
        // remember selected index
        billingsTable.getSelectionModel().selectedIndexProperty().addListener((ov, prevIndex, currentIndex) -> {
            mCurrentBillingIndex = currentIndex.intValue();
        });
        
        billingsTable.getSelectionModel().selectedItemProperty().addListener((ov, b1, b2) -> {
            displaySelectedBilling(b2);
        });
        
        // filter type
        cbFilterType.setItems(FXCollections.observableArrayList("All Types",
                EnumBillingType.WISP.getName(), EnumBillingType.ITEM.getName()
        ));
        
        // filter status
        cbFilterStatus.setItems(FXCollections.observableArrayList("All Status",
                FOR_REVIEW.getName(), FOR_DELIVERY.getName(), DELIVERED.getName(),
                CANCELLED.getName(), FOR_PAYMENT.getName(), PAID.getName(),
                OVERDUE.getName())
        );
        
        // details table & columns
        colDescription.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        paymentsTable.getSelectionModel().selectedItemProperty().addListener((ov, p1, p2) -> {
            disablePaymentActions(p2 == null);
        });
        
        miAddWISPBilling.setOnAction(evt -> showAddWISPBilling());
        miAddItemBilling.setOnAction(evt -> showAddItemBilling());
        
        // WISP actions
        miForPaymentWISP.setOnAction(evt -> {
            Billing billing = billingsTable.getItems().get(mCurrentBillingIndex);
            changeBillingForPayment(billing);
        });
        
        btnEdit.setOnAction(evt -> editBilling());
        btnDelete.setOnAction(evt -> deleteBilling());
        btnRefresh.setOnAction(evt -> refresh());
        
        // details group
        btnAddItem.setOnAction(evt -> showAddPayment());
        btnEditItem.setOnAction(evt -> editBillingPaymentItem());
        btnDeleteItem.setOnAction(evt -> deletePayment());
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
                    
                    // select last selected billing
                    if (mLastBillingIndex > -1) {
                        billingsTable.getSelectionModel().select(mLastBillingIndex);
                        Billing billing = billingsTable.getItems().get(mLastBillingIndex);
//                        displaySelectedBilling(billing);
                    }
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    ErrorDialog.show(String.valueOf(ex.getErrorCode()), ex.toString());
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    private void displaySelectedBilling(Billing billing) {
        disablePaymentsTableAndButtons(billing == null); // first disable/enable details and action buttons
        if (billing != null) {  // if selected is not null
            if (billing.getType() == EnumBillingType.ITEM) { // disable edit for ITEM types
                btnEdit.setDisable(true);
                mbWISP.setDisable(true);
                mbItems.setDisable(false);
            } else {
                if (btnEdit.isDisabled()) btnEdit.setDisable(false); // enable edit for WISP types
                mbWISP.setDisable(false);
                mbItems.setDisable(true);
            }
            if (!detailsGroup.isExpanded()) detailsGroup.setExpanded(true);
            showPaymentDetails(billing);
        } else {
            if (detailsGroup.isExpanded()) detailsGroup.setExpanded(false);
            clearPaymentsTable();
        }
    }
    
    private void showAddItemBilling() {
        if (addItemBillingController != null) {
            if (!addItemBillingController.isLoaded()) addItemBillingController.onLoadTask();
            addItemBillingController.show();
        }
    }

    private void showAddWISPBilling() {
        if (addWISPBillingController != null) {
            if (!addWISPBillingController.isLoaded()) addWISPBillingController.onLoadTask();
            addWISPBillingController.show();
        }
    }
    
    /**
     * Change the status of Billing to For Payment.
     * @param billing 
     */
    private void changeBillingForPayment(Billing billing) {
        if (billing != null) {
            EnumBillingStatus status = billing.getStatus();
            if (status == FOR_REVIEW) {
                QuestionDialog.show("Change Status to \"For Payment\"?",
                        "Once changed, you can never edit or delete this billing entry.",
                        () -> {
                            doChangeBillingStatus(billing, FOR_PAYMENT);
                        });
            } else {
                InfoDialog.show("Oh Snap!", "You can't change the status of this billing entry!");
            }
        }
    }
 
    private void doChangeBillingStatus(Billing billing, EnumBillingStatus status) {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                boolean updated = database.updateBilling(billing.getBillingId(), "status", status.getName());
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    updateBillingTable();
                    if (!updated) {
                        ErrorDialog.show("Oh snap!", "Failed to udpate billing status.");
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
    
    private void editBilling() {
        Billing selected = billingsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getType() == EnumBillingType.WISP) {
                showEditWISPBilling(selected);
            } else {
                showEditItemBilling(selected);
            }
        }
    }
    
    private void showEditWISPBilling(Billing billingSelected) {
        if (billingSelected != null) {
            if (billingSelected.getStatus() == FOR_PAYMENT || billingSelected.getStatus() == FOR_DELIVERY) {
                InfoDialog.show("Unable to update billing!", "The status of this billing "
                        + "doesn't allow the updating of this item.");
            } else {
                if (editWISPBillingController != null) {
                    if (!editWISPBillingController.isLoaded()) editWISPBillingController.onLoadTask();
                    editWISPBillingController.show(billingSelected.getBillingId());
                }
            }
        }
    }
    
    private void showEditItemBilling(Billing billingSelected) {
        
    }
    
    private void deleteBilling() {
        Billing billing = billingsTable.getSelectionModel().getSelectedItem();
        if (billing != null) {
            if (billing.getStatus() == FOR_PAYMENT || billing.getStatus() == FOR_DELIVERY) {
                InfoDialog.show("Unable to delete billing!", "The status of this billing "
                        + "doesn't allow you to delete of this item.");
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, null, ButtonType.YES, ButtonType.NO);
                alert.setTitle("Delete entry?");
                alert.setContentText("You are about to delete this entry. Proceed?");
                Optional<ButtonType> response = alert.showAndWait();
                if (response.isPresent()) {
                    if (response.get() == ButtonType.YES) {
                        doDeleteBilling(billing);
                    } else {
                        alert.close();
                    }
                }
            }
        }
    }

    private void doDeleteBilling(Billing billing) {
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
                        ErrorDialog.show(String.valueOf(ex.getErrorCode()), ex.toString());
                    });
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    private void showAddPayment() {
        Billing selected = billingsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getType() == EnumBillingType.WISP) {
                showAddWISPPayment(selected);
            } else {
                showAddItemPayment(selected);
            }
        }
    }
    
    private void showAddWISPPayment(Billing billing) {
        if (billing.getStatus() == FOR_PAYMENT) {
            InfoDialog.show("Unable to add item!", "The status of this billing "
                    + "doesn't allow adding new item.");
        } else {
            if (addWispController != null) {
                if (!addWispController.isLoaded()) addWispController.onLoadTask();
                addWispController.show(billing);
            }
        }
    }
    
    private void showAddItemPayment(Billing billing) {
        if (billing.getStatus() == FOR_PAYMENT || billing.getStatus() == FOR_DELIVERY) {
            InfoDialog.show("Unable to add item!", "The status of this billing "
                    + "doesn't allow adding new item.");
        } else {
            if (addItemController != null) {
                if (!addItemController.isLoaded()) addItemController.onLoadTask();
                addItemController.show(billing);
            }
        }
    }
    
    private void editBillingPaymentItem() {
        Billing billing = billingsTable.getSelectionModel().getSelectedItem();
        if (billing != null) {
            if (billing.getType() == EnumBillingType.ITEM) {
                showEditItemPayment();
            } else {
                showEditWISPPayment();
            }
        }
    }
    
    /**
     * IMPORTANT! We do not allow the updating of billing's payments when billing's
     * status is not equal to "FOR_REVIEW".
     */
    private void showEditItemPayment() {
        Billing billingSelected = billingsTable.getSelectionModel().getSelectedItem();
        if (billingSelected != null) {
            if (billingSelected.getStatus() == FOR_PAYMENT || billingSelected.getStatus() == FOR_DELIVERY) {
                InfoDialog.show("Unable to update this item!", "The status of this billing "
                        + "doesn't allow the updating of this item.");
            } else {
                Payment paymentSelected = paymentsTable.getSelectionModel().getSelectedItem();
                if (paymentSelected != null) {
                    if (editItemController != null) {
                        if (!editItemController.isLoaded()) editItemController.onLoadTask();
                        editItemController.show(billingSelected, paymentSelected);
                    }
                }
            }
        }
    }
    
    private void showEditWISPPayment() {
        
    }
    
    /**
     * IMPORTANT! We do not allow the deleting of billing's payments when billing's
     * status is not equal to "FOR_REVIEW".
     */
    private void deletePayment() {
        Billing billingSelected = billingsTable.getSelectionModel().getSelectedItem();
        if (billingSelected != null) {
            if (billingSelected.getStatus() == FOR_PAYMENT || billingSelected.getStatus() == FOR_DELIVERY) {
                InfoDialog.show("Unable to delete this item!", "The status of this billing "
                        + "doesn't allow deleting of this item.");
            } else {
                Payment selected = paymentsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, null, ButtonType.YES, ButtonType.NO);
                    alert.setTitle("Delete entry?");
                    alert.setContentText("You are about to delete this entry. Proceed?");
                    Optional<ButtonType> response = alert.showAndWait();
                    if (response.isPresent()) {
                        if (response.get() == ButtonType.YES) {
                            doDeletePayment(selected);
                        } else {
                            alert.close();
                        }
                    }
                }
            }
        }
    }
    
    public void updateBillingTable() {
        mLastBillingIndex = mCurrentBillingIndex;
        refresh();
    }
    
    /**
     * Deletes Payment entry of the selected Billing entry. If deleted successfully,
     * we also update the product inventory IF billing type is of ITEM type.
     * @param payment 
     */
    private void doDeletePayment(Payment payment) {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            if (payment != null) {
                try {
                    Database database = Database.getInstance();
                    boolean deleted = database.deletePayment(payment);
                    if (deleted) {
                        // update inventory
                        Product product = database.findProductByName(payment.getName());
                        if (product != null) {
                            int newProductCount = product.getCount() + payment.getQuantity();
                            database.updateProductCount(product.getProductId(), newProductCount);
                        }
                        
                        // update billing amount
                        Billing billing = billingsTable.getSelectionModel().getSelectedItem();
                        if (billing != null) {
                            billing.removePayment(payment);
                            database.updateBilling(billing.getBillingId(), "amount", billing.getAmount() + "");
                        }
                    }
                    Platform.runLater(() -> {
                        ProgressBarDialog.close();
                        if (!deleted) {
                            ErrorDialog.show("Database Error", "Failed to delete entry.");
                        } else {
                            updateBillingTable();
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
    
    private void disablePaymentsTableAndButtons(boolean disable) {
        btnEdit.setDisable(disable);
        btnDelete.setDisable(disable);
        vboxDetails.setDisable(disable);
    }
    
    private void disablePaymentActions(boolean disable) {
        btnEditItem.setDisable(disable);
        btnDeleteItem.setDisable(disable);
    }
    
    private void showPaymentDetails(Billing billing) {
        paymentsTable.setItems(FXCollections.observableArrayList(billing.getPayments()));
        tfTotal.setText(billing.getAmount() + "");
    }
    
    private void clearPaymentsTable() {
        paymentsTable.getItems().clear();
    }

}
