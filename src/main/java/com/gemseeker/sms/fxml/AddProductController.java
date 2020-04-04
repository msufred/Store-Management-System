package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.AbstractFxmlWindowController;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.data.Product;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class AddProductController extends AbstractFxmlWindowController {

    @FXML TextField tfName;
    @FXML TextField tfPrice;
    @FXML TextField tfCount;
    @FXML TextArea taDescription;
    @FXML Button btnCancel;
    @FXML Button btnSave;
    
    private final InventoryController inventoryController;
    private final CompositeDisposable disposables;
    
    public AddProductController(InventoryController inventoryController) {
        super(AddProductController.class.getResource("add_product.fxml"));
        this.inventoryController = inventoryController;
        disposables = new CompositeDisposable();
    }
    
    @Override
    public void onFxmlLoaded() {
        Utils.setAsNumericalTextFields(tfPrice, tfCount);
        
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
                !tfPrice.getText().isEmpty() &&
                !tfCount.getText().isEmpty();
    }
    
    private void save() {
        ProgressBarDialog.show();
        Product product = getProductInfo();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            boolean added = database.addProduct(product);
            if (added) {
                // add to history
                History history = new History();
                history.setTitle("New Product");
                history.setDescription(String.format("Added new product: %s, amount: Php %.2f, stock: %d", 
                        product.getName(), product.getPrice(), product.getCount()));
                history.setDate(Utils.getDateNow());
                database.addHistory(history);
            }
            return added;
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(added -> {
                    ProgressBarDialog.close();
                    if (!added) {
                        ErrorDialog.show("Database Error", "Failed to add product entry to the database.");
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private Product getProductInfo() {
        Product product = new Product();
        product.setName(tfName.getText());
        product.setPrice(Double.parseDouble(tfPrice.getText()));
        product.setCount(Integer.parseInt(tfCount.getText()));
        product.setDescription(taDescription.getText());
        return product;
    }
}
