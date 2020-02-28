package com.gemseeker.sms.fxml.components;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author gemini1991
 */
public class ProgressBarDialog {
    
    private final ProgressBar progressBar;
    private final VBox vbox;
    private final Stage stage;
    private static ProgressBarDialog instance;
    
    private ProgressBarDialog() {
        progressBar = new ProgressBar();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setMinWidth(500);
        vbox = new VBox();
        vbox.setPadding(new Insets(8));
        vbox.setSpacing(8);
        vbox.getChildren().add(progressBar);
        stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
    }
        
    public static void show() {
        if (instance == null) instance = new ProgressBarDialog();
        instance.stage.show();
    }
    
    public static void close() {
        if (instance != null) instance.stage.close();
    }

}
