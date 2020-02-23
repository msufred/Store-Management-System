package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.core.data.EnumAccountStatus;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Address;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
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
    @FXML TextField tfMonthlyPayment;
    @FXML Button btnGenerate;
    @FXML Button btnSave;
    @FXML Button btnCancel;
    
    private final AccountsController accountsController;
    
    public AddAccountController(AccountsController accountsController) {
        this.accountsController = accountsController;
    }
    
    @Override
    public void onLoadTask() {
        super.onLoadTask();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbDataPlan.setItems(FXCollections.observableArrayList("10", "100", "Unlimited"));
        cbDataPlan.getSelectionModel().select(0);
        
        tfMonthlyPayment.addEventFilter(KeyEvent.KEY_TYPED, evt -> {
            if (!"01234569.".contains(evt.getCharacter())) evt.consume();
        });
        
        btnGenerate.setOnAction(evt -> generateAccountNo());
        
        btnSave.setOnAction(evt -> {
            save();
            close();
            accountsController.refresh();
        });
        btnCancel.setOnAction(evt -> close());
        
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
    
    public void close() {
        if (stage != null) stage.close();
    }
    
    private void generateAccountNo() {
        try {
            Database database = Database.getInstance();
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
        } catch (SQLException ex) {
            ErrorDialog.show(ex.getErrorCode() + "", ex.getLocalizedMessage());
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
            tfContactNo.getText().isEmpty()||
            tfMonthlyPayment.getText().isEmpty()
        );
    }
    
    private void save() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                Account account = getAccountInfo();
                boolean added = database.addAccount(account);
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (!added) {
                        ErrorDialog.show("0x0002", "Failed to add account entry to the dabatase.");
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    ErrorDialog.show(e.getErrorCode() + "", e.getLocalizedMessage());
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    private Account getAccountInfo() {
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
        return newAccount;
    }
}
