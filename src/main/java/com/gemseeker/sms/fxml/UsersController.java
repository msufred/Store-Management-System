package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.User;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author gemini1991
 */
public class UsersController extends Controller {
    
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colPassword;
    @FXML private TableColumn<User, String> colAuthority;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    
    private final CompositeDisposable disposables;
    
    public UsersController() {
        disposables = new CompositeDisposable();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colAuthority.setCellValueFactory(new PropertyValueFactory<>("authority"));
        
        btnAdd.setOnAction(evt -> {
        
        });
        
        btnEdit.setOnAction(evt -> {
        
        });
        
        btnDelete.setOnAction(evt -> {
        
        });
    }

    @Override
    public void onLoadTask() {
        super.onLoadTask();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> Database.getInstance().getAllUsers())
                .subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(users -> {
                    ProgressBarDialog.close();
                    usersTable.setItems(FXCollections.observableArrayList(users));
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
}
