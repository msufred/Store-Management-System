package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import com.gemseeker.sms.Utils;
import static com.gemseeker.sms.data.EnumBillingStatus.*;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Balance;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.BillingProcessed;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingStatus;
import com.gemseeker.sms.data.EnumBillingType;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.data.Product;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.BillingsTableHelper;
import com.gemseeker.sms.fxml.components.InfoDialog;
import com.gemseeker.sms.fxml.components.PaymentsTableHelper;
import com.gemseeker.sms.fxml.components.QuestionDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import com.gemseeker.sms.fxml.print.PrintController;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author gemini1991
 */
public class BillingsController extends Controller {
    
    // <editor-fold desc="FXML Components" defaultstate="collapsed">
    // basic actions
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    
    // add billing actions
    @FXML private MenuItem miAddWISPBilling;
    @FXML private MenuItem miAddItemBilling;
    
    // WISP actions
    @FXML private MenuButton mbWISP;
    @FXML private MenuItem miForPaymentWISP;
    @FXML private MenuItem miReceivePaymentWISP;
    @FXML private MenuItem miExtendDueDateWISP;
    
    // ITEM actions
    @FXML private MenuButton mbItems;
    @FXML private MenuItem miReceivePaymentItem;
    @FXML private MenuItem miExtendDueDateItem;
    @FXML private MenuItem miDeliverOrder;
    @FXML private MenuItem miCancelOrder;
    
    // PRINT actions
    @FXML private MenuButton mbPrint;
    @FXML private MenuItem miPrintStatement;
    @FXML private MenuItem miPrintNotification;
    @FXML private MenuItem miPrintReceipt;
    
    // Filter Actions
    @FXML private ComboBox<String> cbSortBy;
    @FXML private ComboBox<String> cbMonth;
    @FXML private ComboBox<String> cbYear;
    
    @FXML private TableView<Billing> billingsTable;
    
    // details group (billing breakdown)
    @FXML private TitledPane detailsGroup;
    @FXML private VBox vboxDetails;
    @FXML private TableView<Payment> paymentsTable;
    @FXML private Button btnAddItem;
    @FXML private Button btnEditItem;
    @FXML private Button btnDeleteItem;
    @FXML private TextField tfBalance;
    @FXML private TextField tfSubtotal;
    @FXML private TextField tfTotal;
    // </editor-fold>
    
    private ContextMenu wispContextMenu;
    private MenuItem cmForPayment;
    private MenuItem cmReceivePayment;
    private MenuItem cmExtendDueDate;
    private MenuItem cmPrintNotice;
    private MenuItem cmPrintStatement;
    private MenuItem cmPrintReceipt;

    private AddWISPBillingController addWISPBillingController;
    private EditWISPBillingController editWISPBillingController;
    private AddItemBillingController addItemBillingController;
    
    private AddItemController addItemController;
    private EditItemController editItemController;
    private AddWISPController addWISPController;
    private EditWISPController editWISPController;
    
    // WISP specific dialogs
    private ChangeDueDateController changeDueDateController;
    private AcceptPaymentController acceptPaymentController;
    
    private PrintController printController;
    private PrintNoticeController printNoticeController;
    private ReceiptFormController receiptFormController;
    private NoticeFormController noticeFormController;
    
    private int mCurrentBillingIndex = -1; // current selected Billing entry in table
    private int mLastBillingIndex = -1; // holds the last index selected (can be used after refresh)
    
    // holds the Billing entries from the database
    // value change every refresh
    private ArrayList<Billing> mBillings;
    
    private final CompositeDisposable disposables;
    
    public BillingsController() {
        disposables = new CompositeDisposable();
    }
    
    @Override
    public void onLoadTask() {
        super.onLoadTask();
        addWISPBillingController = new AddWISPBillingController(this);
        editWISPBillingController = new EditWISPBillingController(this);
        addItemBillingController = new AddItemBillingController(this);
        
        addItemController = new AddItemController(this);
        editItemController = new EditItemController(this);
        addWISPController = new AddWISPController(this);
        editWISPController = new EditWISPController(this);
        
        changeDueDateController = new ChangeDueDateController(this);
        acceptPaymentController = new AcceptPaymentController(this);
        
        printController = new PrintController();
        printNoticeController = new PrintNoticeController(this);
        receiptFormController = new ReceiptFormController();
        noticeFormController = new NoticeFormController();
        
        Loader loader = Loader.getInstance();
        loader.load("fxml/add_wisp_billing.fxml", addWISPBillingController);
        loader.load("fxml/edit_wisp_billing.fxml", editWISPBillingController);
        loader.load("fxml/add_item_billing.fxml", addItemBillingController);
        
        loader.load("fxml/add_item.fxml", addItemController);
        loader.load("fxml/edit_item.fxml", editItemController);
        loader.load("fxml/add_wisp.fxml", addWISPController);
        loader.load("fxml/edit_wisp.fxml", editWISPController);
        
        loader.load("fxml/change_due_date.fxml", changeDueDateController);
        loader.load("fxml/accept_payment.fxml", acceptPaymentController);
        
        loader.load("fxml/print.fxml", printController);
        loader.load("fxml/notice.fxml", printNoticeController);
        loader.load("fxml/receipt2.fxml", receiptFormController);
        loader.load("fxml/notice2.fxml", noticeFormController);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // setup context menus
        setupBillingsContextMenu();
        
        // setup billings table
        BillingsTableHelper.setupTable(billingsTable);
        
        // remember selected index
        billingsTable.getSelectionModel().selectedIndexProperty()
                .addListener((ov, prevIndex, currentIndex) -> {
            mCurrentBillingIndex = currentIndex.intValue();
        });
        
        billingsTable.getSelectionModel().selectedItemProperty()
                .addListener((ov, b1, b2) -> {
            displaySelectedBilling(b2);
        });
        
        // adding context menus to billingsTable
        billingsTable.setContextMenu(wispContextMenu);
        
        cbMonth.setItems(Utils.getMonthsList());
        cbMonth.valueProperty().addListener(o -> filterTableByMonthYear());
        
        cbYear.setItems(Utils.getYearsList());
        cbYear.valueProperty().addListener(o -> filterTableByMonthYear());
        
        PaymentsTableHelper.setupTable(paymentsTable);
        
        paymentsTable.getSelectionModel().selectedItemProperty().addListener((ov, p1, p2) -> {
            disablePaymentActions(p2 == null);
        });
        
        miAddWISPBilling.setOnAction(evt -> showAddWISPBilling());
        miAddItemBilling.setOnAction(evt -> showAddItemBilling());
        
        // WISP actions
        miForPaymentWISP.setOnAction(evt -> changeBillingForPayment());
        miExtendDueDateWISP.setOnAction(evt -> extendDueDate());
        miReceivePaymentWISP.setOnAction(evt -> acceptPayment());
        
        // ITEM actions
        miReceivePaymentItem.setOnAction(evt -> acceptPayment());
        miCancelOrder.setOnAction(evt -> cancelOrder());
        miDeliverOrder.setOnAction(evt -> deliverOrder());
        miExtendDueDateItem.setOnAction(evt -> extendDueDate());
        
        btnEdit.setOnAction(evt -> editBilling());
        btnDelete.setOnAction(evt -> deleteBilling());
        btnRefresh.setOnAction(evt -> {
            reEvaluateBillings();
            refresh();
        });
        
        miPrintNotification.setOnAction(evt -> printNotification());
        miPrintStatement.setOnAction(evt -> printStatement());
        miPrintReceipt.setOnAction(evt -> printReceipt());
        
        // details group
        btnAddItem.setOnAction(evt -> showAddPayment());
        btnEditItem.setOnAction(evt -> editBillingPaymentItem());
        btnDeleteItem.setOnAction(evt -> deletePayment());
    }
    
    private void setupBillingsContextMenu() {
        wispContextMenu = new ContextMenu();
        
        cmForPayment = new MenuItem("For Payment");
        cmForPayment.setOnAction(evt -> changeBillingForPayment());
        
        cmReceivePayment = new MenuItem("Receive Payment");
        cmReceivePayment.setOnAction(evt -> acceptPayment());
        
        cmExtendDueDate = new MenuItem("Extend Due Date");
        cmExtendDueDate.setOnAction(evt -> extendDueDate());
        
        cmPrintNotice = new MenuItem("Print Notice");
        cmPrintNotice.setOnAction(evt -> printNotification());
        
        cmPrintStatement = new MenuItem("Print Statement");
        cmPrintStatement.setOnAction(evt -> printStatement());
        
        cmPrintReceipt = new MenuItem("Print Receipt");
        cmPrintReceipt.setOnAction(evt -> printReceipt());
        
        wispContextMenu.getItems().addAll(cmForPayment, cmReceivePayment,
                cmExtendDueDate, cmPrintNotice, cmPrintStatement, cmPrintReceipt);
    }
    
    @Override
    public void onResume() {
        super.onResume(); 
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        addWISPBillingController.onDestroy();
        editWISPBillingController.onDestroy();
        addItemBillingController.onDestroy();
        
        addItemController.onDestroy();
        editItemController.onDestroy();
        addWISPController.onDestroy();
        editWISPController.onDestroy();
        
        changeDueDateController.onDestroy();
        acceptPaymentController.onDestroy();
        
        printController.onDestroy();
        printNoticeController.onDestroy();
        receiptFormController.onDestroy();
        noticeFormController.onDestroy();
        disposables.dispose();
    }

    public void refresh() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
                Database database = Database.getInstance();
                return database.getAllBillings();
            }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                    .subscribe((billings) -> { 
                        ProgressBarDialog.close();
                        billingsTable.setItems(FXCollections.observableArrayList(billings));

                        // select last selected billing
                        if (mLastBillingIndex > -1) {
                            billingsTable.getSelectionModel().select(mLastBillingIndex);
                        }

                        // this depends if user filtered table by month and year
                        filterTableByMonthYear();
                    }, err -> {
                        if (err.getCause() != null) {
                            ProgressBarDialog.close();
                            ErrorDialog.show("Fetch Billings Error", err.getLocalizedMessage());
                        }
                    })
        );
    }
    
    /**
     * Re-evaluates Billings entry. This is to check the due dates. If it is due
     * or overdue, Billing entry is updated. This method is called before refresh.
     */
    private void reEvaluateBillings() {
        ProgressBarDialog.show();
        disposables.add(Completable.fromCallable(() -> {
            Database database = Database.getInstance();
            if (mBillings == null) {
                mBillings = database.getAllBillings();
            }

            ArrayList<Billing> toUpdate = new ArrayList<>();
            for (Billing b : mBillings) {
                if (b.getStatus() != EnumBillingStatus.OVERDUE &&
                        b.getStatus() != EnumBillingStatus.FOR_REVIEW &&
                        b.getStatus() != EnumBillingStatus.PAID) {
                    Date date = Utils.DATE_FORMAT_2.parse(b.getDueDate());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    Calendar now = Calendar.getInstance();

                    if (Utils.compare(cal, now) < 0) {
                        toUpdate.add(b);
                    }
                }
            }

            for (Billing b : toUpdate) {
                database.updateBilling(b.getBillingId(), "status", EnumBillingStatus.OVERDUE.getName());
            }
            return null;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(() -> {
                    ProgressBarDialog.close();
                }, err -> {
                    ProgressBarDialog.close();
                    ErrorDialog.show("Error while re-evalation billings.", err.getLocalizedMessage());
                })
        );
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
    
    private void disablePaymentsTableAndButtons(boolean disable) {
        btnEdit.setDisable(disable);
        btnDelete.setDisable(disable);
        vboxDetails.setDisable(disable);
        mbPrint.setDisable(disable);
    }
    
    private void disablePaymentActions(boolean disable) {
        btnEditItem.setDisable(disable);
        btnDeleteItem.setDisable(disable);
    }
    
    private void showPaymentDetails(Billing billing) {
        paymentsTable.setItems(FXCollections.observableArrayList(billing.getPayments()));
        
        /*
        If billing is not paid, we will display previous balance then add it
        to the actual billing amount. 
         */
        Account account = billing.getAccount();
        double tBalance = 0; 
        if (billing.getStatus() != PAID) {
            if (account != null) {
                ArrayList<Balance> balances = account.getBalances();
                if (!balances.isEmpty()) {
                    for (Balance b : balances) {
                        if (!b.isIsPaid()) {
                            tBalance += b.getAmount();
                        }
                    }
                }
                
            }
        }
        tfBalance.setText(tBalance + "");
        tfSubtotal.setText(billing.getAmount() + "");
        tfTotal.setText(String.valueOf(tBalance + billing.getAmount()));
    }
    
    private void clearPaymentsTable() {
        paymentsTable.getItems().clear();
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
    
    
    /* ======================================================================= */
    /*                       Billing Actions (WISP & Item)                     */
    /* ======================================================================= */
    
    /**
     * Change the status of Billing to For Payment.
     * @param billing 
     */
    private void changeBillingForPayment() {
        Billing billing = billingsTable.getItems().get(mCurrentBillingIndex);
        if (billing != null) {
            EnumBillingStatus status = billing.getStatus();
            if (status == FOR_REVIEW) {
                QuestionDialog.show("Change Status to \"For Payment\"?",
                        "Once changed, you can never edit or delete this billing entry.",
                        () -> {
                            doChangeBillingStatus(billing, FOR_PAYMENT);
                        });
            } else {
                InfoDialog.show("Can't Update Billing", "The status of this billing doesn't allow it "
                        + "to be updated!");
            }
        }
    }
 
    private void doChangeBillingStatus(Billing billing, EnumBillingStatus status) {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            boolean updated = database.updateBilling(billing.getBillingId(), "status", status.getName());
            if (updated) {
                History history = new History();
                history.setDate(Utils.getDateNow());
                history.setTitle("Changed Billing Status");
                history.setDescription(String.format("Change status of billing [ID: %d] to %s",
                        billing.getBillingId(), status.getName()));
                database.addHistory(history);
            }
            return updated;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(updated -> {
                    ProgressBarDialog.close();
                    updateBillingTable();
                    if (!updated) {
                        ErrorDialog.show("Change Status Error", "Failed to udpate billing status.");
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Change Status Error", err.getLocalizedMessage());
                    }
                }));
    }
    
    private void acceptPayment() {
        Billing billing = billingsTable.getItems().get(mCurrentBillingIndex);
        if (billing != null) {
            EnumBillingStatus status = billing.getStatus();
            if (status != PAID && status != CANCELLED) {
                if (!acceptPaymentController.isLoaded()) acceptPaymentController.onLoadTask();
                acceptPaymentController.show(billing);
            } else {
                InfoDialog.show("Can't Accept Payment", "This billing is either paid or cancelled.");
            }
        }
    }
    
    private void printNotification() {
        if (billingsTable.getSelectionModel().getSelectedIndex() > -1) {
            Billing billing = getSelectedBilling();
            if (billing != null) {
                EnumBillingStatus status = billing.getStatus();
                if (status != PAID && status != CANCELLED) {
//                    if (!printNoticeController.isLoaded()) printNoticeController.onLoadTask();
//                    printNoticeController.show(billing);
                    if (!noticeFormController.isLoaded()) noticeFormController.onLoadTask();
                    noticeFormController.clear();
                    noticeFormController.setBilling(billing);

                    if (!printController.isLoaded()) printController.onLoadTask();
                    printController.show(noticeFormController.getContentPane());
                } else {
                    InfoDialog.show("Can't Print Notification", "You can only print notice "
                            + "statement if billing is not paid, cancelled or delivered.");
                }
            }
        }
    }
    
    private void printStatement() {
        if (billingsTable.getSelectionModel().getSelectedIndex() > -1) {
        Billing billing = getSelectedBilling();
            if (billing != null) {
                if (billing.getStatus() != PAID && billing.getStatus() != CANCELLED) {
                    // TODO
                }
            }
        }
    }
    
    private void printReceipt() {
        if (billingsTable.getSelectionModel().getSelectedIndex() > -1) {
            Billing billing = getSelectedBilling();
            if (billing != null) {
                EnumBillingStatus status = billing.getStatus();
                if (status != PAID) {
                    InfoDialog.show("Can't Print Receipt", "This billing must be paid first before printing a receipt.");
                } else {
                    doPrintReceipt(billing.getBillingId(), billing.getDateUpdated());
                }
            }
        }
    }
    
    private void doPrintReceipt(int billingId, Date dateTransaction) {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            return database.getBillingProcessed(billingId, dateTransaction);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(bp -> {
                    ProgressBarDialog.close();
                    if (bp != null) {
                        printReceipt(bp);
                    } else {
                        ErrorDialog.show("Error Printing Receipt", "Failed to print receipt.");
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Error Printing Receipt", err.getLocalizedMessage());
                    }
                }));
    }
    
    public void printReceipt(BillingProcessed billingProcessed) {
        if (billingProcessed != null) {
            if (!receiptFormController.isLoaded()) receiptFormController.onLoadTask();
            receiptFormController.clear();
            receiptFormController.setBillingProcessed(billingProcessed);

            if (!printController.isLoaded()) printController.onLoadTask();
            printController.show(receiptFormController.getContentPane());
        }
    }
    
    private void cancelOrder() {
        if (billingsTable.getSelectionModel().getSelectedIndex() > -1) {
            Billing billing = getSelectedBilling();
            if (billing != null) {
                if (billing.getStatus() != PAID && billing.getStatus() != CANCELLED &&
                        billing.getStatus() != DELIVERED) {
                    QuestionDialog.show("Cancel Order?", "Click YES to cancel order or NO to close this dialog.", () -> {
                        doCancelOrder(billing);
                    }, "YES", "NO");
                } else {
                    ErrorDialog.show("Can't Cancel Order", "You can't cancel order if it is already paid,"
                            + " delivered or cancelled.");
                }
            }
        }
    }
    
    private void doCancelOrder(Billing billing) {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            boolean updated = database.updateBilling(billing.getBillingId(), "status", CANCELLED.getName());
            if (updated) {
                History history = new History();
                history.setDate(Utils.getDateNow());
                history.setTitle("Cancel Billing");
                history.setDescription(String.format("Cancelled billing with ID: %s", billing.getBillingId()));
                database.addHistory(history);
            }
            return updated;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(updated -> {
                    ProgressBarDialog.close();
                    if (!updated) {
                        ErrorDialog.show("Failed to Cancel Order", "Error while cancelling order.");
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Failed to Cancel Order", err.getLocalizedMessage());
                    }
                }));
    }
    
    private void deliverOrder() {
        Billing billing = getSelectedBilling();
        if (billing != null) {
            if (billing.getStatus() != PAID && billing.getStatus() != CANCELLED &&
                    billing.getStatus() != DELIVERED) {
                QuestionDialog.show("Change status to DELIVERED?", "Click YES to deliver order or NO to close this dialog.", () -> {
                    doDeliverOrder(billing);
                }, "YES", "NO");
            } else {
                ErrorDialog.show("Can't set status to DELIVERED", "You can't set order status to DELIVERED if it is already paid,"
                        + " delivered or cancelled.");
            }
        }
    }
    
    private void doDeliverOrder(Billing billing) {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            boolean updated = database.updateBilling(billing.getBillingId(), "status", DELIVERED.getName());
            if (updated) {
                History history = new History();
                history.setDate(Utils.getDateNow());
                history.setTitle("Order Delivered");
                history.setDescription(String.format("Delivered order for billing with ID: %s", billing.getBillingId()));
                database.addHistory(history);
            }
            return updated;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(updated -> {
                    ProgressBarDialog.close();
                    if (!updated) {
                        ErrorDialog.show("Failed to set status to DELIVERED", "Error while changing order status to DELIVERED.");
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Failed to set status to DELIVERED", err.getLocalizedMessage());
                    }
                }));
    }
    
    private void extendDueDate() {
        Billing billing = getSelectedBilling();
        if (billing != null) {
            EnumBillingStatus status = billing.getStatus();
            if (status != PAID && status != CANCELLED && status != DELIVERED) {
                if (changeDueDateController != null) {
                    if (!changeDueDateController.isLoaded()) changeDueDateController.onLoadTask();
                    changeDueDateController.show(billing);
                }
            } else {
                InfoDialog.show("Unable to extend due date.", "This billing is either paid or cancelled.");
            }
        }
    }
    
    private Billing getSelectedBilling() {
        return billingsTable.getItems().get(mCurrentBillingIndex);
    }
    
    /* ======================================================================= */
    /*                                  END                                    */
    /* ======================================================================= */    
    
  
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
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            boolean deleted = database.deleteBilling(billing);
            if (deleted) {
                History history = new History();
                history.setDate(Utils.getDateNow());
                history.setTitle("Delete Billing");
                history.setDescription(String.format("Deleted billing with ID: %s", billing.getBillingId()));
                database.addHistory(history);
            }
            return deleted;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(deleted -> {
                    ProgressBarDialog.close();
                    if (!deleted) {
                        ErrorDialog.show("Delete Billing Error", "Failed to delete entry.");
                    }
                    refresh();
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Delete Billing Error", err.getLocalizedMessage());
                    }
                }));
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
            if (addWISPController != null) {
                if (!addWISPController.isLoaded()) addWISPController.onLoadTask();
                addWISPController.show(billing);
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
        Billing billingSelected = billingsTable.getSelectionModel().getSelectedItem();
        if (billingSelected != null) {
            if (billingSelected.getStatus() == FOR_PAYMENT || billingSelected.getStatus() == FOR_DELIVERY) {
                InfoDialog.show("Unable to update this item!", "The status of this billing "
                        + "doesn't allow the updating of this item.");
            } else {
                Payment paymentSelected = paymentsTable.getSelectionModel().getSelectedItem();
                if (paymentSelected != null) {
                    if (editWISPController != null) {
                        if (!editWISPController.isLoaded()) editWISPController.onLoadTask();
                        editWISPController.show(billingSelected, paymentSelected);
                    }
                }
            }
        }
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
        disposables.add(Observable.fromCallable(() -> {
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

                    // add to history
                    History history = new History();
                    history.setDate(Utils.getDateNow());
                    history.setTitle("Billing Item Delete");
                    history.setDescription(String.format("Item [%s] for billing [ID: %d] deleted.",
                            payment.getName(), billing.getBillingId()));
                    database.addHistory(history);
                }
            }
            return deleted;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(deleted -> {
                    ProgressBarDialog.close();
                    if (!deleted) {
                        ErrorDialog.show("Delete Error", "Failed to delete entry.");
                    } else {
                        updateBillingTable();
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Delete Error", err.getLocalizedMessage());
                    }
                }));
    }
    
    private void filterTableByMonthYear() {
        if ((cbMonth.getValue() == null || cbYear.getValue() == null) ||
                cbMonth.getValue().equals("All") && cbYear.getValue().equals("All")) {
            billingsTable.setItems(FXCollections.observableArrayList(mBillings));
        } else {
            String month = cbMonth.getValue();
            String year = cbYear.getValue();
            ArrayList<Billing> filtered = new ArrayList<>();
            for (Billing b : mBillings) {
                Calendar billingDate = Calendar.getInstance();
                billingDate.setTime(b.getBillingDate());
                
                if (month.equals("All Months")) {
                    if (billingDate.get(Calendar.YEAR) == Integer.parseInt(year)) {
                        filtered.add(b);
                    }
                } else if (year.equals("All Years")) {
                    if (billingDate.get(Calendar.MONTH) == Utils.monthIntegerValue(month) - 1) {
                        filtered.add(b);
                    }
                } else if (billingDate.get(Calendar.MONTH) == Utils.monthIntegerValue(month) - 1 &&
                        billingDate.get(Calendar.YEAR) == Integer.parseInt(year)) {
                    filtered.add(b);
                }
            }
            billingsTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

}
