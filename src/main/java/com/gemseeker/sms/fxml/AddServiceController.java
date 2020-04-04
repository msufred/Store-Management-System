package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.AbstractFxmlWindowController;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.data.Service;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class AddServiceController extends AbstractFxmlWindowController {

    @FXML TextField tfName;
    @FXML TextField tfPrice;
    @FXML TextArea taDesc;
    @FXML Button btnSave;
    @FXML Button btnCancel;

    private final InventoryController inventoryController;
    private final CompositeDisposable disposables;
    
    public AddServiceController(InventoryController inventoryController) {
        super(AddServiceController.class.getResource("add_service.fxml"));
        this.inventoryController = inventoryController;
        disposables = new CompositeDisposable();
    }
    
    @Override
    public void onFxmlLoaded() {
        Utils.setAsNumericalTextFields(tfPrice);
        
        btnCancel.setOnAction(evt -> closeWindow());
        
        btnSave.setOnAction(evt -> {
            if (fieldsValidated()) {
                save();
                closeWindow();
                inventoryController.refresh();
            }
        });
    }

    @Override
    public void onCloseRequest(WindowEvent windowEvent) {
        disposables.dispose();
    }
    
    private boolean fieldsValidated() {
        return !tfName.getText().isEmpty() &&
                !tfPrice.getText().isEmpty();
    }
    
    private void save() {
        ProgressBarDialog.show();
        Service service = getServiceInfo();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            boolean added = database.addService(service);
            if (added) {
                // add to history
                History history = new History();
                history.setTitle("New Service");
                history.setDescription(String.format("Added new service: %s, est. amount: Php %.2f", 
                        service.getName(), service.getEstPrice()));
                history.setDate(Utils.getDateNow());
                database.addHistory(history);
            }
            return added;
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(added -> {
                    ProgressBarDialog.close();
                    if (!added) {
                        ErrorDialog.show("Database Error", "Failed to add service entry to the database.");
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private Service getServiceInfo() {
        Service service = new Service();
        service.setName(tfName.getText());
        service.setEstPrice(Double.parseDouble(tfPrice.getText()));
        service.setDescription(taDesc.getText());
        return service;
    }
}
