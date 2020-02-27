package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Address;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumAccountStatus;
import com.gemseeker.sms.data.InternetSubscription;
import com.gemseeker.sms.fxml.components.AccountNameTableCell;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 *
 * @author gemini1991
 */
public class AccountsController extends Controller {

    @FXML Button btnAdd;
    @FXML Button btnEdit;
    @FXML Button btnDelete;
    @FXML Button btnRefresh;
    @FXML Button btnChangeStatus;
    
    @FXML TableView<Account> accountsTable;
    @FXML TableColumn<Account, String> colAccountNo;
    @FXML TableColumn<Account, String> colName;
    @FXML TableColumn<Account, Address> colAddress;
    @FXML TableColumn<Account, String> colContact;
    @FXML TableColumn<Account, EnumAccountStatus> colStatus;
    @FXML Label labelEmpty;
    @FXML VBox detailsContent;
    
    // account details group
    @FXML TitledPane detailsGroupPane;
    @FXML Label lblAccountNo;
    @FXML Label lblName;
    @FXML Label lblAddress;
    @FXML Label lblContact;
    @FXML Label lblStatus;
    @FXML Label lblType;
    
    // subscription group
    @FXML TitledPane internetGroupPane;
    @FXML Label lblBandwidth;
    @FXML Label lblAmount;
    @FXML Label lblIPAddress;
    @FXML Label lblLongitude;
    @FXML Label lblLatitude;
    @FXML Label lblElevation;
    
    private AddAccountController addAccountController;

    /**
     * Initialize controllers; load FXMLs
     */
    @Override
    public void onLoadTask() {
        super.onLoadTask();
        addAccountController = new AddAccountController(this);
        
        Loader loader = Loader.getInstance();
        loader.load("fxml/add_account.fxml", addAccountController);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colAccountNo.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colName.setCellFactory(col -> new AccountNameTableCell());
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
//        accountsTable.setRowFactory(row -> new AccountTableRow());
        accountsTable.getSelectionModel().selectedItemProperty().addListener((o, a1, a2) -> {
            detailsContent.setVisible(a2 != null);
            labelEmpty.setVisible(a2 == null);
            disableActions(a2 == null);
            if (a2 != null) {
                showAccountDetails(a2);
                if (!detailsGroupPane.isExpanded() && !internetGroupPane.isExpanded()) {
                    detailsGroupPane.setExpanded(true);
                }
            }
        });
        
        btnAdd.setOnAction(evt -> {
            if (addAccountController != null) {
                if (!addAccountController.isLoaded()) addAccountController.onLoadTask();
                addAccountController.show();
            }
        });
        
        btnDelete.setOnAction(evt -> {
            Account account = accountsTable.getSelectionModel().getSelectedItem();
            if(account != null) {
                Alert alert = new Alert(AlertType.WARNING, null, ButtonType.CANCEL, ButtonType.YES);
                alert.setTitle("Delete Account?");
                alert.setHeaderText("Are you sure you want to delete this account?");
                alert.setContentText("Deleting this account will also delete billing entries "
                        + "and other data related to this account!");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.isPresent()) {
                    if (option.get() == ButtonType.YES) {
                        deleteAccount(account);
                    } else {
                        alert.close();
                    }
                }
            }
        });
        
        btnRefresh.setOnAction(evt -> refresh());
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
                ArrayList<Account> accts = database.getAllAccounts();
                Platform.runLater(() -> {
                    accountsTable.setItems(FXCollections.observableArrayList(accts));
                    ProgressBarDialog.close();
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
    
    private void disableActions(boolean disable) {
        btnEdit.setDisable(disable);
        btnDelete.setDisable(disable);
        btnChangeStatus.setDisable(disable);
    }
    
    private void showAccountDetails(Account account) {
        lblAccountNo.setText(account.getAccountNumber());
        lblName.setText(String.format("%s %s", account.getFirstName(), account.getLastName()));
        Address address = account.getAddress();
        if (address != null) {
            lblAddress.setText(address.toString());
        } else {
            lblAddress.setText("<Not Set>");
        }
        lblContact.setText(account.getContactNumber());
        lblStatus.setText(account.getStatus().toString());
        lblType.setText(account.getAccountType().toString());
        
        // Internet Subscription
        InternetSubscription sub = account.getInternetSubscription();
        if (sub != null) {
            lblBandwidth.setText(sub.getBandwidth() + "");
            lblAmount.setText(sub.getAmount() + "");
            lblIPAddress.setText(sub.getIpAddress());
            lblLatitude.setText(sub.getLatitude() + "");
            lblLongitude.setText(sub.getLongitude() + "");
            lblElevation.setText(sub.getElevation() + "");
        } else {
            lblBandwidth.setText("<Not Set>");
            lblAmount.setText("<Not Set>");
            lblIPAddress.setText("<Not Set>");
            lblLatitude.setText("<Not Set>");
            lblLongitude.setText("<Not Set>");
            lblElevation.setText("<Not Set>");
        }
    }

    private void deleteAccount(Account account) {
        if (account == null) {
            ErrorDialog.show("Oh snap!", "No selected account.");
            return;
        }
        
        ProgressBarDialog.show(); 
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                boolean deleted = database.deleteAccount(account);
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (deleted) {
                        refresh();
                    } else {
                        ErrorDialog.show("Account Delete Error", "Failed to delete account.");
                    }
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    ErrorDialog.show("Account Delete Error", "Failed to delete account.");
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
