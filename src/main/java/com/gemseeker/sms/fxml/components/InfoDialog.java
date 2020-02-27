package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.Loader;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class InfoDialog {

    @FXML private Button btnOK;
    @FXML private Label headerLabel;
    @FXML private Label contentLabel;
    
    private Stage stage;

    private static InfoDialog instance;
    
    //private final Alert dialog;
    
    private InfoDialog() {
        //dialog = new Alert(AlertType.INFORMATION);
        init();
    }
    
    private void init() {
        Loader loader = Loader.getInstance();
        Pane root = loader.load("fxml/info_dialog.fxml", this);
        
        stage = new Stage();
        stage.setTitle("Info");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        
        btnOK.setOnAction(evt -> {
            doClose();
        });
    }
    
    public void setHeaderText(String headerText) {
        if (headerText != null) {
            headerLabel.setText(headerText);
        }
    }
    
    public void setContentText(String contentText) {
        if (contentText != null) {
            contentLabel.setText(contentText);
        }
    }
    
    public void doShow() {
        if (stage != null) stage.show();
    }
    
    public void doClose() {
        if (stage != null) stage.close();
    }

    public static void show(String header, String content) {
        if (instance == null) instance = new InfoDialog();
        instance.setHeaderText(header);
        instance.setContentText(content);
        instance.doShow();
    }

}
