package com.gemseeker.sms.fxml;

import com.gemseeker.sms.core.AbstractPanelController;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProductsTable;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import com.gemseeker.sms.fxml.components.ServicesTable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.net.URL;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author gemini1991
 */
public class InventoryController extends AbstractPanelController {
    
    @FXML VBox vbox;
    @FXML ChoiceBox<String> cbSelection;
    @FXML Button btnAdd;
    @FXML Button btnEdit;
    @FXML Button btnDelete;
    @FXML Button btnRefresh;
    
    private ProductsTable productsTable;
    private ServicesTable servicesTable;
    
    private boolean isProduct = true;
    
    private final AddProductController addProductController = new AddProductController(this);
    private final AddServiceController addServiceController = new AddServiceController(this);
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void makePanel() {
        final URL fxmlURL = InventoryController.class.getResource("inventory.fxml");
        final FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setLocation(fxmlURL);
        try {
            setPanel(loader.load());
            controllerDidLoadFxml();
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("Failed to load " + fxmlURL.getFile());
        }
    }
    
    private void controllerDidLoadFxml() {
        assert getPanel() != null;
        
        // Init components and listeners
        productsTable = new ProductsTable();
        VBox.setVgrow(productsTable, Priority.ALWAYS);
        productsTable.getSelectionModel().selectedItemProperty().addListener((o, p1, p2) -> {
            disableActions(p2 == null);
        });
        
        servicesTable = new ServicesTable();
        VBox.setVgrow(servicesTable, Priority.ALWAYS);
        servicesTable.getSelectionModel().selectedItemProperty().addListener((o, s1, s2) -> {
            disableActions(s2 == null);
        });
        
        cbSelection.setItems(FXCollections.observableArrayList("Products", "Services"));
        cbSelection.getSelectionModel().selectedIndexProperty().addListener((o, prevIndex, currIndex) -> {
            if (currIndex.intValue() > -1) {
                disableActions(true);
                if (currIndex.intValue() == 0) {
                    isProduct = true;
                    vbox.getChildren().remove(servicesTable);
                    vbox.getChildren().add(productsTable);
                    getProducts();
                } else {
                    isProduct = false;
                    vbox.getChildren().remove(productsTable);
                    vbox.getChildren().add(servicesTable);
                    getServices();
                }
            }
        });
        cbSelection.getSelectionModel().select(0);
        
        btnAdd.setOnAction(evt -> {
            if (isProduct) {
                addProductController.openWindow();
            } else {
                addServiceController.openWindow();
            }
        });
    }
    
    @Override
    public void dispose() {
        super.dispose();
        disposables.dispose();
    }

    public void refresh() {
        int index = cbSelection.getSelectionModel().getSelectedIndex();
        if (index > -1) {
            if (index == 0) {
                servicesTable.getItems().clear();
                getProducts();
            } else {
                productsTable.getItems().clear();
                getServices();
            }
        }
    }
    
    private void getProducts() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> Database.getInstance().getAllProducts())
                .subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(products -> {
                    ProgressBarDialog.close();
                    productsTable.setItems(FXCollections.observableArrayList(products));
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private void getServices() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> Database.getInstance().getAllServices())
                .subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(services -> {
                    ProgressBarDialog.close();
                    servicesTable.setItems(FXCollections.observableArrayList(services));
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private void disableActions(boolean disable) {
        btnEdit.setDisable(disable);
        btnDelete.setDisable(disable);
    }
    
}
