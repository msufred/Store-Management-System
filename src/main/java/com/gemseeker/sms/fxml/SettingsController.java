package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Preferences;
import com.gemseeker.sms.core.AbstractFxmlWindowController;
import com.gemseeker.sms.data.User;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class SettingsController extends AbstractFxmlWindowController {

    @FXML private VBox databaseGroup;
    @FXML private TextField tfDatabaseName;
    @FXML private TextField tfDatabaseHost;
    @FXML private TextField tfDatabaseURL;
    @FXML private TextField tfUserName;
    @FXML private TextField tfPassword;
    @FXML private Button btnConnection;
    @FXML private Label lblConnection;
    
    @FXML private Button btnCancel;
    @FXML private Button btnSave;
    
    private User mUser;
    
    public SettingsController() {
        super(SettingsController.class.getResource("settings.fxml"));
    }

    @Override
    protected void onFxmlLoaded() {
        btnCancel.setOnAction(evt -> closeWindow());
        btnSave.setOnAction(evt -> {
            // close for now
            closeWindow();
        });
    }

    @Override
    public void onCloseRequest(WindowEvent windowEvent) {
        
    }
    
    public void openWindow(User user) {
        openWindow(); // AbstractFxmlWindowController
        clearFields();
        loadPreferences();
        if (user.getAuthority().equals("administrator")) {
            databaseGroup.setDisable(false);
        }
    }

    private void loadPreferences() {
        Preferences pref = Preferences.getInstance();
        tfDatabaseName.setText(pref.getDatabaseName());
        tfDatabaseHost.setText(pref.getDatabaseHost());
        tfDatabaseURL.setText(pref.getDatabaseURL());
        tfUserName.setText(pref.getDatabaseUser());
        tfPassword.setText(pref.getDatabasePassword());
    }
    
    private void clearFields() {
        tfDatabaseName.clear();
        tfDatabaseHost.clear();
        tfDatabaseURL.clear();
        tfUserName.clear();
        tfPassword.clear();
        databaseGroup.setDisable(true);
    }
}
