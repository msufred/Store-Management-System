package com.gemseeker.sms;

import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.User;
import com.gemseeker.sms.fxml.MainController;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.InfoDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class AppMain extends Application implements Initializable {

    private static final String TITLE = "LIVErary IT Solutions";
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 700;
    
    private static final String DEBUG_NAME = "AppMain";
    private static final boolean LOG_TO_FILE = false; // change to log to file

    // login
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Button loginBtn;
    @FXML private Button exitBtn;
    @FXML private ProgressBar progressBar;
    private Pane loginPane;
    private StackPane root;
    
    // controllers
    private MainController mainController;
    
    private static Logger logger;
    
    @Override
    public void start(Stage stage) throws Exception {
        logger = new Logger(LOG_TO_FILE);
        
        root = new StackPane();
        root.setStyle("-fx-background-color: #ffffff");
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
//        stage.setMaximized(true);
        stage.setTitle(TITLE);
        stage.setOnCloseRequest(evt -> {
            try {
                Database database = Database.getInstance();
                database.close();
            } catch (SQLException ex) {
                ErrorDialog.show(ex.getErrorCode() + "", ex.getLocalizedMessage());
            }
        });
        
        logger.log(DEBUG_NAME, "Starting application!");
        stage.show();

        loadPanels();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userField.textProperty().addListener((o, s1, s2) -> validateInput());
        passField.textProperty().addListener((o, s1, s2) -> validateInput());
        loginBtn.setOnAction(evt -> validateLogin());
        exitBtn.setOnAction(evt -> System.exit(0));
    }
    
    private void validateInput() {
        loginBtn.setDisable(userField.getText().isEmpty() || passField.getText().isEmpty());
    }
    
    private void validateLogin() {
        // for now, proceed to dashboard
        progressBar.setVisible(true);
        String username = userField.getText();
        String password = passField.getText();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                User user = database.getUser(username, password);
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    if (user != null) {
                        boolean isValid = user.getUserName().equals(username) &&
                                user.getPassword().equals(password);
                        if (isValid) {
                            mainController.setUser(user);
                            changeController(mainController);
                        } else {
                            InfoDialog.show("Invalid User", "Please enter correct username and password.");
                            userField.clear();
                            passField.clear();
                            userField.requestFocus();
                        }
                    } else {
                        InfoDialog.show("Invalid User", "Please enter correct username and password.");
                        userField.clear();
                        passField.clear();
                        userField.requestFocus();
                    }
                });
            } catch (SQLException ex) {
                progressBar.setVisible(false);
                
                ErrorDialog.show(ex.getErrorCode() + "", ex.getLocalizedMessage());
                logger.logErr(DEBUG_NAME, "failed to validate login username and password", ex);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void loadPanels() {
        logger.log(DEBUG_NAME, "loading fxml controllers...");
        // show progress bar while loading panels
        ProgressBarDialog.show();
        
        Loader loader = Loader.getInstance();
        
        // initialize controllers
        mainController = new MainController();
        
        // load login
        loginPane = loader.load("fxml/login.fxml", this);
        
        // load other panels
        loader.load("fxml/main.fxml", mainController);
        
        // load login panel...
        root.getChildren().clear();
        root.getChildren().add(loginPane);
        
        ProgressBarDialog.close();
    }
    
    public void changeController(Controller controller) {
        logger.log(DEBUG_NAME, "changing controller to " + controller.getClass().getName());
        
        Parent panel = controller.getContentPane();
        if (panel == null) {
            logger.log(DEBUG_NAME, "controller's content pane is null");
            return;
        }
        root.getChildren().clear();
        root.getChildren().add(panel);
        controller.onLoadTask();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public static Logger getLogger() {
        if (logger == null) logger = new Logger(LOG_TO_FILE);
        return logger;
    }
}
