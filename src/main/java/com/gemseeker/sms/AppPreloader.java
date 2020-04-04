package com.gemseeker.sms;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author gemini1991
 */
public class AppPreloader extends Preloader {
    
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        final ImageView splash = new ImageView();
        splash.setImage(new Image(getClass().getResource("fxml/splash.png").toString()));
        final StackPane root = new StackPane();
        root.setPadding(new Insets(16));
        root.getChildren().add(splash);
        final Scene scene = new Scene(root, 400, 200);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
        this.stage = stage;
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        super.handleProgressNotification(pn);
        
        assert stage != null;
        
        if (pn.getProgress() == 1.0) {
            Platform.runLater(() -> {
                stage.close();
            });
        }
    }
}
