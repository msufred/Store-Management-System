package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class BillingsController extends Controller {
    
    @FXML Button btnAdd;
    @FXML Button btnEdit;
    @FXML Button btnDelete;
    @FXML Button btnRefresh;
    @FXML TableView<Billing> billingsTable;
    @FXML TableColumn<Billing, String> colDateOfBilling;
    @FXML TableColumn<Billing, String> colAccountNo;
    @FXML TableColumn<Billing, String> colName;
    @FXML TableColumn<Billing, String> colDueDate;
    @FXML TableColumn<Billing, Double> colAmount;
    @FXML TableColumn<Billing, String> colStatus;

    private AddBillingController addBillingController;
    
    @Override
    public void onLoadTask() {
        super.onLoadTask();
        addBillingController = new AddBillingController();
        
        Loader loader = Loader.getInstance();
        loader.load("fxml/addpayment.fxml", addBillingController);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colAccountNo.setCellValueFactory(new PropertyValueFactory<>("accountNo"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        btnAdd.setOnAction(evt -> {
            if (addBillingController != null) {
                if (!addBillingController.isLoaded()) addBillingController.onLoadTask();
                addBillingController.show();
            }
        });
        btnRefresh.setOnAction(evt -> onResume());
    }

    @Override
    public void onResume() {
        super.onResume(); 
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                ArrayList billings = database.getAllBillings();
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    billingsTable.setItems(FXCollections.observableArrayList(billings));
                });
            } catch (SQLException ex) {
                Logger.getLogger(BillingsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        t.setDaemon(true);
        t.start();
    }

}
