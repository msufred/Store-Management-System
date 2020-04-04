package com.gemseeker.sms.fxml;

import com.gemseeker.seekiconsfx.material.HistoryIcon;
import com.gemseeker.seekiconsfx.material.PersonIcon;
import com.gemseeker.seekiconsfx.material.PesoIcon;
import com.gemseeker.seekiconsfx.material.SettingsIcon;
import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import com.gemseeker.sms.core.AbstractPanelController;
import com.gemseeker.sms.data.User;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
public class MainController extends AbstractPanelController {

    @FXML private ToggleButton btnDashboard;
    @FXML private ToggleButton btnCustomers;
    @FXML private ToggleButton btnPayments;
    @FXML private ToggleButton btnSales;
    @FXML private ToggleButton btnHistory;
    @FXML private ToggleButton btnInventory;
    @FXML private ToggleButton btnUsers;
    @FXML private StackPane contentPane;
    @FXML private Label lblAuthority;
    @FXML private Button btnSettings;
    
    // icons from seekiconsfx
    private static final double ICON_SIZE = 14;
    private static final double SIDEBAR_ICON_SIZE = 18;
    private final SettingsIcon settingsIcon;
    private final PesoIcon pesoIcon;
    private final PersonIcon personIcon;
    private final PersonIcon personIcon2;
    private final HistoryIcon historyIcon;
    
    private ToggleGroup paymentsToggleGroup;
    
    // controllers
    // -- store controllers
    
    // -- payments monitoring controllers
    private final SummaryController summaryController = new SummaryController();
    private final BillingsController billingsController = new BillingsController();
    private final AccountsController accountssController = new AccountsController();
    private final SalesController salesController = new SalesController();
    private final InventoryController inventoryController = new InventoryController();
    private final UsersController usersController = new UsersController();
    private final HistoryController historyController = new HistoryController();
    
    private final SettingsController settingsController = new SettingsController();
    
    // ...
    private AbstractPanelController mCurrentController = null;
    
    private User mUser;
    
    public MainController() {
        settingsIcon = new SettingsIcon(ICON_SIZE);
        pesoIcon = new PesoIcon(ICON_SIZE);
        pesoIcon.getStyleClass().add("sidebar-history-icon");
        personIcon = new PersonIcon(ICON_SIZE);
        personIcon.getStyleClass().add("sidebar-history-icon");
        personIcon2 = new PersonIcon(ICON_SIZE);
        personIcon2.getStyleClass().add("sidebar-history-icon");
        historyIcon = new HistoryIcon(SIDEBAR_ICON_SIZE);
        historyIcon.getStyleClass().add("sidebar-history-icon");
    }

    @Override
    protected void makePanel() {
        final URL fxmlURL = MainController.class.getResource("main.fxml");
        final FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setLocation(fxmlURL);
        try {
            setPanel(loader.load());
            controllerDidLoadFxml();
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("Failed to load " + fxmlURL.getFile());
        }
    }
    
    private void controllerDidLoadFxml() {
        assert getPanel() != null;
        assert contentPane != null;
        assert btnDashboard != null;
        assert btnCustomers != null;
        assert btnPayments != null;
        assert btnSales != null;
        assert btnInventory != null;
        assert btnUsers != null;
        assert btnHistory != null;
        
        // Init components & listeners
        paymentsToggleGroup = new ToggleGroup();
        paymentsToggleGroup.getToggles().addAll(btnDashboard, btnCustomers,
                btnPayments, btnSales, btnHistory, btnInventory, btnUsers);
        setupToggles();
        
        setupTogglesEventFilter(btnDashboard, btnCustomers, btnPayments, 
                btnSales, btnHistory, btnInventory, btnUsers);
        
        btnSettings.setGraphic(settingsIcon);
        btnSettings.setOnAction(evt -> settingsController.openWindow(mUser));
        

        // load primary panel
        changeView(summaryController);
        summaryController.refresh();
    }
    
    public void refresh() {
        if (mUser != null) {
            if (mUser.getAuthority().equals("administrator")) {
                lblAuthority.setText("Administrator");
            } else {
                lblAuthority.setText("Guest");
                btnUsers.setVisible(false);
            }
        }
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
        // add icons
        btnSales.setGraphic(pesoIcon);
        btnCustomers.setGraphic(personIcon);
        btnUsers.setGraphic(personIcon2);
        btnHistory.setGraphic(historyIcon);
        
        paymentsToggleGroup.selectedToggleProperty().addListener((ov, t, t1) -> {
            if (t1.equals(btnDashboard)) {
                changeView(summaryController);
                summaryController.refresh();
            } else if (t1.equals(btnPayments)) {
                changeView(billingsController);
                billingsController.refresh();
            } else if (t1.equals(btnCustomers)) {
                changeView(accountssController);
                accountssController.refresh();
            } else if (t1.equals(btnSales)) {
                changeView(salesController);
                salesController.refresh();
            } else if (t1.equals(btnHistory)) {
                changeView(historyController);
                historyController.refresh();
            } else if (t1.equals(btnInventory)) {
                changeView(inventoryController);
                inventoryController.refresh();
            } else if (t1.equals(btnUsers)) {
                changeView(usersController);
                usersController.refresh();
            }
        });
    }
    
    private void changeView(AbstractPanelController controller) {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(controller.getPanel());
        mCurrentController = controller;
    }

    public void setUser(User user) {
        mUser = user;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        billingsController.dispose();
        accountssController.dispose();
        salesController.dispose();
        inventoryController.dispose();
        usersController.dispose();
        historyController.dispose();
//        settingsController.dispose();
    }
    
}
