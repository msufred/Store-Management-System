package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Address;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author gemini1991
 */
public class AccountsController extends Controller {

    @FXML Button btnAdd;
    @FXML Button btnEdit;
    @FXML Button btnDelete;
    @FXML Button btnRefresh;
    @FXML TableView<Account> accountsTable;
    @FXML TableColumn<Account, String> colAccountNo;
    @FXML TableColumn<Account, String> colFirstName;
    @FXML TableColumn<Account, String> colLastName;
    @FXML TableColumn<Account, Address> colAddress;
    @FXML TableColumn<Account, String> colContact;
    @FXML TableColumn<Account, Integer> colDataPlan;
    @FXML TableColumn<Account, String> colStatus;
    
    private AddAccountController addAccountController;
    
    private Database database;
    
    @Override
    public void onLoadTask() {
        super.onLoadTask();
        addAccountController = new AddAccountController();
        
        Loader loader = Loader.getInstance();
        loader.load("fxml/add_account.fxml", addAccountController);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colAccountNo.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colDataPlan.setCellValueFactory(new PropertyValueFactory<>("dataPlan"));
        colDataPlan.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer dataPlan, boolean empty) {
                super.updateItem(dataPlan, empty); 
                if (!empty) {
                    if (dataPlan == 0) setText("Unlimited");
                    else setText(String.format("%d mbps", dataPlan));
                }
            }
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        btnAdd.setOnAction(evt -> {
            if (addAccountController != null) {
                if (!addAccountController.isLoaded()) addAccountController.onLoadTask();
                addAccountController.show();
            }
        });
        
        btnRefresh.setOnAction(evt -> {
            onResume();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                if (database == null) database = Database.getInstance();
                ArrayList<Account> accts = database.getAllAccounts();
                Platform.runLater(() -> {
                    accountsTable.setItems(FXCollections.observableArrayList(accts));
                    ProgressBarDialog.close();
                });
            } catch (SQLException ex) {
                System.err.println("Failed to get database entrt. " + ex);
            }
        });
        t.setDaemon(true);
        t.start();
    }

}
