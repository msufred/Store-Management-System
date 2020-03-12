package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Preferences;
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

/**
 *
 * @author gemini1991
 */
public class SettingsController extends Controller {

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
    
    private Stage stage;
    private Scene scene;
    
    private User mUser;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        btnCancel.setOnAction(evt -> close());
        btnSave.setOnAction(evt -> {
            // close for now
            close();
        });
    }

    public void show(User user) {
        clearFields();
        if (stage == null) {
            initStage();
        }
        stage.show();
        loadPreferences();
        if (user.getAuthority().equals("administrator")) {
            databaseGroup.setDisable(false);
        }
    }
    
    public void close() {
        if (stage != null) stage.close();
    }
    
    private void initStage() {
        stage = new Stage();
        stage.setTitle("Settings");
        stage.initModality(Modality.APPLICATION_MODAL);
        scene = new Scene(getContentPane());
        stage.setScene(scene);
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
