package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import com.gemseeker.sms.data.User;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 *
 * @author gemini1991
 */
public class MainController extends Controller {

    @FXML private ToggleButton btnDashboard;
    @FXML private ToggleButton btnCustomers;
    @FXML private ToggleButton btnPayments;
    @FXML private ToggleButton btnSales;
    @FXML private ToggleButton btnHistory;
    @FXML private ToggleButton btnInventory;
    @FXML private ToggleButton btnUsers;
    @FXML private StackPane paymentsContentPane;
    @FXML private Label lblAuthority;
    @FXML private Button btnSettings;
    
    private ToggleGroup paymentsToggleGroup;
    
    // controllers
    // -- store controllers
    
    // -- payments monitoring controllers
    private SummaryController summaryController;
    private BillingsController billingsController;
    private AccountsController accountssController;
    private SalesController salesController;
    private InventoryController inventoryController;
    private UsersController usersController;
    private HistoryController historyController;
    
    private SettingsController settingsController;
    
    // ...
    private Controller mCurrentController = null;
    
    private User mUser;

    @Override
    public void onLoadTask() {
        super.onLoadTask();
        summaryController = new SummaryController();
        billingsController = new BillingsController();
        accountssController = new AccountsController();
        salesController = new SalesController();
        inventoryController = new InventoryController();
        usersController = new UsersController();
        historyController = new HistoryController();
        
        settingsController = new SettingsController();
        
        loadControllers();
        
        // todo load initial controller
        changeView(summaryController);
        
        if (mUser != null) {
            if (mUser.getAuthority().equals("administrator")) {
                lblAuthority.setText("Administrator");
            } else {
                lblAuthority.setText("Guest");
                btnUsers.setVisible(false);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {      
        paymentsToggleGroup = new ToggleGroup();
        paymentsToggleGroup.getToggles().addAll(btnDashboard, btnCustomers,
                btnPayments, btnSales, btnHistory, btnInventory, btnUsers);
        setupToggles();
        
        setupTogglesEventFilter(btnDashboard, btnCustomers, btnPayments, 
                btnSales, btnHistory, btnInventory, btnUsers);
        
        btnSettings.setOnAction(evt -> {
            if (!settingsController.isLoaded()) settingsController.onLoadTask();
            settingsController.show(mUser);
        });
    }
    
    private void loadControllers() {
        Loader loader = Loader.getInstance();
        
        // load payments controllers
        loader.load("fxml/summary.fxml", summaryController);
        loader.load("fxml/billings.fxml", billingsController);
        loader.load("fxml/accounts.fxml", accountssController);
        loader.load("fxml/sales.fxml", salesController);
        loader.load("fxml/inventory.fxml", inventoryController);
        loader.load("fxml/users.fxml", usersController);
        loader.load("fxml/history.fxml", historyController);
        
        loader.load("fxml/settings.fxml", settingsController);
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
            } else if (t1.equals(btnSales)) {
                changeView(salesController);
            } else if (t1.equals(btnHistory)) {
                changeView(historyController);
            } else if (t1.equals(btnInventory)) {
                changeView(inventoryController);
            } else if (t1.equals(btnUsers)) {
                changeView(usersController);
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

    public void setUser(User user) {
        mUser = user;
    }
    
    public void dispose() {
        summaryController.onDestroy();
        billingsController.onDestroy();
        accountssController.onDestroy();
        salesController.onDestroy();
        inventoryController.onDestroy();
        usersController.onDestroy();
        historyController.onDestroy();
        settingsController.onDestroy();
    }
    
}
