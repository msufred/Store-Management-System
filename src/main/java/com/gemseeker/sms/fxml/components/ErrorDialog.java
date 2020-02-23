package com.gemseeker.sms.fxml.components;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author gemini1991
 */
public class ErrorDialog {

    private static ErrorDialog instance;
    private final Alert dialog;
    
    private ErrorDialog() {
        dialog = new Alert(AlertType.ERROR);
    }
    
    public static void show(String errorCode, String errorMessage) {
        if (instance == null) instance = new ErrorDialog();
        instance.dialog.setTitle("Error: " + errorCode);
        instance.dialog.setHeaderText(errorCode);
        instance.dialog.setContentText(errorMessage);
        instance.dialog.showAndWait();
    }
    
    public static void close() {
        if (instance != null) instance.dialog.close();
    }
}
