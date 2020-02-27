package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.Service;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class AddServiceController extends Controller {

    @FXML TextField tfName;
    @FXML TextField tfPrice;
    @FXML TextArea taDesc;
    @FXML Button btnSave;
    @FXML Button btnCancel;
    
    private Stage stage;
    private Scene scene;
    
    private final InventoryController inventoryController;
    
    public AddServiceController(InventoryController inventoryController) {
        this.inventoryController = inventoryController;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Utils.setAsNumericalTextFields(tfPrice);
        
        btnCancel.setOnAction(evt -> {
            if (stage != null) stage.close();
        });
        
        btnSave.setOnAction(evt -> {
            if (fieldsValidated()) {
                save();
                close();
                inventoryController.refresh();
            }
        });
    }

    public void show() {
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Add Service");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(true);
            
            scene = new Scene(getContentPane());
            stage.setScene(scene);
        }
        stage.show();
    }
    
    public void close() {
        if (stage != null) stage.close();
    }
    
    private boolean fieldsValidated() {
        return !tfName.getText().isEmpty() &&
                !tfPrice.getText().isEmpty();
    }
    
    private void save() {
        ProgressBarDialog.show();
        Service service = getServiceInfo();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                boolean added = database.addService(service);
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (!added) {
                        ErrorDialog.show("Database Error", "Failed to add service entry to the database.");
                    }
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    ErrorDialog.show(ex.getErrorCode() + "", ex.getLocalizedMessage());
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    private Service getServiceInfo() {
        Service service = new Service();
        service.setName(tfName.getText());
        service.setEstPrice(Double.parseDouble(tfPrice.getText()));
        service.setDescription(taDesc.getText());
        return service;
    }
}
