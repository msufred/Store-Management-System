package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.core.data.EnumAccountStatus;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Address;
import com.gemseeker.sms.data.Database;
import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class AddAccountController extends Controller {

    private Stage stage;
    private Scene scene;
    @FXML TextField tfAccountNo;
    @FXML TextField tfFirstname;
    @FXML TextField tfLastname;
    @FXML TextField tfBuilding;
    @FXML TextField tfStreet;
    @FXML TextField tfCity;
    @FXML TextField tfContactNo;
    @FXML ChoiceBox<String> cbDataPlan;
    @FXML Button btnGenerate;
    @FXML Button btnSave;
    @FXML Button btnCancel;
    
    private Database database;
    
    @Override
    public void onLoadTask() {
        super.onLoadTask();
        try {
            database = Database.getInstance();
        } catch (SQLException ex) {
            System.err.println("Failed to get database instance.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbDataPlan.setItems(FXCollections.observableArrayList("10", "100", "Unlimited"));
        cbDataPlan.getSelectionModel().select(0);
        
        btnGenerate.setOnAction(evt -> generateAccountNo());
        btnSave.setOnAction(evt -> save());
        btnCancel.setOnAction(evt -> {
            if (stage != null) stage.close();
        });
        
        setupTextFields(tfFirstname, tfLastname, tfBuilding, tfStreet, tfCity, tfContactNo);
    }

    public void show() {
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Add Account");
            if (scene == null) scene = new Scene(getContentPane());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.show();
    }
    
    private void generateAccountNo() {
        if (database != null) {
            int count = database.getAccountsCount();
            if (count == 0) {
                tfAccountNo.setText("ACCT-0");
            } else {
                String accntNo;
                while(true) {
                    accntNo = "ACCT-" + count;
                    if (database.hasAccountNo(accntNo)) {
                        count++;
                    } else {
                        break;
                    }
                }
                tfAccountNo.setText(accntNo);
            }
        }
    }
    
    private void setupTextFields(TextField...textFields) {
        for (TextField tf: textFields) {
            tf.textProperty().addListener((ov, t, t1) -> {
                validate();
            });
        }
    }
    
    private void validate() {
        btnSave.setDisable(
            tfAccountNo.getText().isEmpty() ||
            tfFirstname.getText().isEmpty() ||
            tfLastname.getText().isEmpty() ||
            tfBuilding.getText().isEmpty() ||
            tfStreet.getText().isEmpty() ||
            tfCity.getText().isEmpty() ||
            tfContactNo.getText().isEmpty()
        );
    }
    
    private void save() {
        Account newAccount = new Account();
        newAccount.setAccountNumber(tfAccountNo.getText());
        String firstName = tfFirstname.getText();
        String lastName = tfLastname.getText();
        newAccount.setAccountUserName(lastName, firstName);
        Address address = new Address();
        address.setStreet(tfBuilding.getText());
        address.setBarangay(tfStreet.getText());
        address.setCity(tfCity.getText());
        newAccount.setAddress(address);
        newAccount.setContactNumber(tfContactNo.getText());
        String dataPlanStr = cbDataPlan.getValue();
        int dataPlan;
        if (dataPlanStr.equals("Unlimited")) {
            dataPlan = 0;
        } else {
            try {
                dataPlan = Integer.parseInt(dataPlanStr);
            } catch (NumberFormatException e) {
                dataPlan = 0;
            }
        }
        newAccount.setDataPlan(dataPlan);
        newAccount.setDateRegistered(Calendar.getInstance().getTime());
        newAccount.setStatus(EnumAccountStatus.ACTIVE);
        
        try {
            if (database == null) database = Database.getInstance();
            boolean added = database.addAccount(newAccount);
            if (added) {
                if (stage != null) stage.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to save account entry.");
        }
    }
}
