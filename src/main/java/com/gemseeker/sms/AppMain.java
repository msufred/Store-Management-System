package com.gemseeker.sms;

import com.gemseeker.sms.fxml.MainController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Application;
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

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;

    // login
    @FXML TextField userField;
    @FXML PasswordField passField;
    @FXML Button loginBtn;
    @FXML Button exitBtn;
    private Pane loginPane;
    private StackPane root;
    private ProgressBar progressBar;
    
    private boolean hasUserInput = false;
    private boolean hasPassInput = false;
    
    // controllers
    private MainController mainController;
    
    @Override
    public void start(Stage stage) throws Exception {
        progressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setPrefWidth(200);

        root = new StackPane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        loadPanels();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userField.textProperty().addListener((o, s1, s2) -> {
            hasUserInput = !s2.isBlank();
            validateInput();
        });
        passField.textProperty().addListener((o, s1, s2) -> {
            hasPassInput = !s2.isBlank();
            validateInput();
        });
        loginBtn.setOnAction(evt -> validateLogin());
        exitBtn.setOnAction(evt -> System.exit(0));
    }
    
    private void validateInput() {
        loginBtn.setDisable(!hasUserInput && !hasPassInput);
    }
    
    private void validateLogin() {
        // for now, proceed to dashboard
        changeController(mainController);
    }

    private void loadPanels() {
        // show progress bar while loading panels
        root.getChildren().clear();
        root.getChildren().add(progressBar);

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
    }
    
    public void changeController(Controller controller) {
        Parent panel = controller.getContentPane();
        if (panel == null) {
            System.out.println("content pane is null");
            return;
        }
        root.getChildren().clear();
        root.getChildren().add(panel);
        controller.onLoadTask();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
