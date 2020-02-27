package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 *
 * @author gemini1991
 */
public class MainController extends Controller {

    @FXML ToggleButton btnDashboard;
    @FXML ToggleButton btnCustomers;
    @FXML ToggleButton btnReports;
    @FXML ToggleButton btnPayments;
    @FXML ToggleButton btnSales;
    @FXML ToggleButton btnTransactions;
    @FXML ToggleButton btnInventory;
    @FXML StackPane paymentsContentPane;
    
    private ToggleGroup paymentsToggleGroup;
    
    // controllers
    // -- store controllers
    
    // -- payments monitoring controllers
    private SummaryController summaryController;
    private BillingsController billingsController;
    private AccountsController accountssController;
    private SalesController salesController;
    private ReportsController reportsController;
    private TransactionsController transactionsController;
    private InventoryController inventoryController;
    
    // ...
    private Controller mCurrentController = null;

    @Override
    public void onLoadTask() {
        super.onLoadTask();
        summaryController = new SummaryController();
        billingsController = new BillingsController();
        accountssController = new AccountsController();
        salesController = new SalesController();
        reportsController = new ReportsController();
        transactionsController = new TransactionsController();
        inventoryController = new InventoryController();
        
        loadControllers();
        
        // todo load initial controller
        changeView(summaryController);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {      
        paymentsToggleGroup = new ToggleGroup();
        paymentsToggleGroup.getToggles().addAll(btnDashboard, btnCustomers, btnReports,
                btnPayments, btnSales, btnTransactions, btnInventory);
        setupToggles();
        
        setupTogglesEventFilter(btnDashboard, btnCustomers, btnReports, btnPayments, 
                btnSales, btnTransactions, btnInventory);
    }
    
    private void loadControllers() {
        Loader loader = Loader.getInstance();
        
        // load payments controllers
        loader.load("fxml/summary.fxml", summaryController);
        loader.load("fxml/billings.fxml", billingsController);
        loader.load("fxml/accounts.fxml", accountssController);
        loader.load("fxml/sales.fxml", salesController);
        loader.load("fxml/reports.fxml", reportsController);
        loader.load("fxml/transactions.fxml", transactionsController);
        loader.load("fxml/inventory.fxml", inventoryController);
    }

    /**
     * Sets ToggleButtons to consume click event if it is already selected.
     * @param toggles 
     */
    private void setupTogglesEventFilter(ToggleButton...toggles) {
        for (ToggleButton toggle : toggles) {
            toggle.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (toggle.isSelected()) event.consume();
            });
        }
    }
    
    private void setupToggles() {
        paymentsToggleGroup.selectedToggleProperty().addListener((ov, t, t1) -> {
            if (t1.equals(btnDashboard)) {
                changeView(summaryController);
            } else if (t1.equals(btnPayments)) {
                changeView(billingsController);
            } else if (t1.equals(btnCustomers)) {
                changeView(accountssController);
            } else if (t1.equals(btnReports)) {
                changeView(reportsController);
            } else if (t1.equals(btnSales)) {
                changeView(salesController);
            } else if (t1.equals(btnTransactions)) {
                changeView(transactionsController);
            } else if (t1.equals(btnInventory)) {
                changeView(inventoryController);
            }
        });
    }
    
    private void changeView(Controller controller) {
        if (!controller.isLoaded()) controller.onLoadTask();
        paymentsContentPane.getChildren().clear();
        paymentsContentPane.getChildren().add(controller.getContentPane());
        mCurrentController = controller;
        controller.onResume();
    }

}
