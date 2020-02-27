package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.Loader;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class QuestionDialog {
    
    public static interface Command {
        public void call();
    }
    
    @FXML HBox buttonGroup;
    @FXML private Button btnOK;
    @FXML private Button btnCancel;
    @FXML private Label headerLabel;
    @FXML private Label contentLabel;
    
    private Stage stage;
    
    private Command command;
    
    private static QuestionDialog instance;
    
    //private final Alert dialog;
    
    private QuestionDialog() {
        //dialog = new Alert(AlertType.INFORMATION);
        init();
    }
    
    private void init() {
        Loader loader = Loader.getInstance();
        Pane root = loader.load("fxml/question_dialog.fxml", this);
        
        stage = new Stage();
        stage.setTitle("Question Dialog");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        
        btnOK.setOnAction(evt -> {
            if (command != null) {
                command.call();
            }
            doClose();
        });
        
        btnCancel.setOnAction(evt -> {
            if (stage != null) stage.close();
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
    
    public void setOnProceed(Command command) {
        this.command = command;
    }
    
    public void setButtonText(String okButtonText, String cancelButtonText) {
        if (okButtonText != null) {
            btnOK.setText(okButtonText);
        }
        if (cancelButtonText == null) {
            buttonGroup.getChildren().remove(btnCancel);
        } else {
            if (!buttonGroup.getChildren().contains(btnCancel)) {
                buttonGroup.getChildren().add(btnCancel);
            }
            btnCancel.setText(cancelButtonText);
        }
    }
    
    public void doShow() {
        if (stage != null) stage.show();
    }
    
    public void doClose() {
        if (stage != null) stage.close();
    }
    
//    public static void show(String header, String content) {
//        if (instance == null) instance = new InfoDialog();
//        instance.dialog.setTitle("Info");
//        instance.dialog.setHeaderText(header);
//        instance.dialog.setContentText(content);
//        instance.dialog.showAndWait();
//    }
    
//    public static void close() {
//        if (instance != null) instance.dialog.close();
//    }
    
    public static void show(String header, String content, Command onProceed) {
        show(header, content, onProceed, null, null);
    }
    
    public static void show(String header, String content, Command onProceed, String okButtonText) {
        show(header, content, onProceed, okButtonText, null);
    }
    
    public static void show(String header, String content, Command onProceed, String okButtonText, String cancelButtonText) {
        if (instance == null) instance = new QuestionDialog();
        instance.setHeaderText(header);
        instance.setContentText(content);
        instance.setOnProceed(onProceed);
        instance.setButtonText(okButtonText, cancelButtonText);
        instance.doShow();
    }

}
