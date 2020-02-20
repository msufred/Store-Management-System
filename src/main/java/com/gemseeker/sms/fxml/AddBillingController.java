package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Database;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class AddBillingController extends Controller {

    private Stage stage;
    private Scene scene;
    
    @FXML ChoiceBox<Account> cbAccounts;
    @FXML ChoiceBox<String> cbMonths;
    @FXML ChoiceBox<Integer> cbYears;
    @FXML ComboBox<String> cbPaymentType;
    @FXML TextField tfData;
    @FXML TextField tfBalance;
    @FXML TextField tfDesc;
    @FXML TextField tfAmount;
    @FXML TextField tfQuantity;
    @FXML Button btnSavePrint;
    @FXML Button btnSaveOnly;
    @FXML Button btnCancel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbAccounts.valueProperty().addListener((ov, t, t1) -> {
            if (t1 != null) {
                Platform.runLater(() -> {
                    tfData.setText(String.valueOf(t1.getDataPlan()));
                    // TODO show balance...
                });
            }
        });
        cbMonths.setItems(FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"
        ));
        cbMonths.getSelectionModel().select(0);
        
        cbYears.setItems(FXCollections.observableArrayList(2020, 2021, 2022, 2023, 2024, 2025));
        cbYears.getSelectionModel().select(0);
        
        cbPaymentType.setItems(FXCollections.observableArrayList(
                "Data Plan", "Installation", "Re-installation", "Beaming", "Others"
        ));
        
        tfAmount.addEventFilter(KeyEvent.KEY_TYPED, evt -> {
            String num = "0123456789.";
            if (!num.contains(evt.getCharacter())) evt.consume();
        });
        tfQuantity.addEventFilter(KeyEvent.KEY_TYPED, evt -> {
            String num = "0123456789.";
            if (!num.contains(evt.getCharacter())) evt.consume();
        });
        
        btnSavePrint.setOnAction(evt -> {
            
        });
        
        btnSaveOnly.setOnAction(evt -> {
        
        });
        
        btnCancel.setOnAction(evt -> {
            if (stage != null) stage.close();
        });
    }

    @Override
    public void onLoadTask() {
        super.onLoadTask(); 
    }

    public void show() {
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Add Billing");
            if (scene == null) scene = new Scene(getContentPane());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.show();
        loadAccounts();
    }
    
    private void loadAccounts() {
        try {
            Database database = Database.getInstance();
            ArrayList<Account> accts = database.getAllAccounts();
            cbAccounts.setItems(FXCollections.observableArrayList(accts));
        } catch (SQLException ex) {
            Logger.getLogger(AddBillingController.class.getName()).log(Level.SEVERE, null, ex);
            if (stage != null) stage.close();
        }
    }
}
