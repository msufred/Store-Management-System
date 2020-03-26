package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import static com.gemseeker.sms.data.EnumBillingStatus.*;
import com.gemseeker.sms.data.EnumBillingType;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingStatus;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.data.Payment;
import com.gemseeker.sms.data.Product;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ResourceBundle;
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

    @FXML private ComboBox<Account> cbAccounts;
    @FXML private HBox addItemGroup;
    @FXML private TableView<Payment> itemsTable;
    @FXML private TableColumn<Payment, String> colItem;
    @FXML private TableColumn<Payment, Double> colPrice;
    @FXML private TableColumn<Payment, Integer> colQuantity;
    @FXML private TableColumn<Payment, Double> colTotal;
    @FXML private ComboBox<Product> cbItems;
    @FXML private TextField tfPrice;
    @FXML private Spinner spQuantity;
    @FXML private TextField tfTotal;
    @FXML private DatePicker dueDate;
    @FXML private ChoiceBox<EnumBillingStatus> cbStatus;
    @FXML private Button btnAdd;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;
    @FXML private Button btnSavePrint;
    
    private Stage stage;
    private Scene scene;
    
    private final BillingsController billingsController;
    private final CompositeDisposable disposables;
    
    public AddItemBillingController(BillingsController billingsController) {
        this.billingsController = billingsController;
        disposables = new CompositeDisposable();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
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
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            ArrayList<Product> products = database.getAllProducts();
            ArrayList<Account> accounts = database.getAllAccounts();
            return Arrays.asList(products, accounts);
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(entriesList -> {
                    ProgressBarDialog.close();
                    cbItems.setItems(FXCollections.observableArrayList((ArrayList<Product>) entriesList.get(0)));
                    cbAccounts.setItems(FXCollections.observableArrayList((ArrayList<Account>) entriesList.get(1)));
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
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
        disposables.add(Observable.fromCallable(() -> {
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

                // add to history
                History history = new History();
                history.setTitle("New Item Billing");
                history.setDescription(String.format("Added new ITEM billing amounting to Php %.2f, due on %s",
                        billing.getAmount(), billing.getDueDate()));
                history.setDate(Utils.getDateNow());
                database.addHistory(history);
            }
            return added;
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(added -> {
                    ProgressBarDialog.close();
                    if (!added) ErrorDialog.show("Database Error", "Failed to add billing entry to the database.");
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
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
