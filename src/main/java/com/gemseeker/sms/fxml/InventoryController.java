package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.Product;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
public class InventoryController extends Controller {
    
    @FXML TableView<Product> itemsTable;
    @FXML TableColumn<Product, Integer> colItemNo;
    @FXML TableColumn<Product, String> colName;
    @FXML TableColumn<Product, Double> colPrice;
    @FXML TableColumn<Product, Integer> colCount;
    @FXML Button btnAdd;
    @FXML Button btnEdit;
    @FXML Button btnDelete;
    @FXML Button btnRefresh;
    
    private AddProductController addProductController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colItemNo.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        
        btnAdd.setOnAction(evt -> {
            if (addProductController != null) {
                if (!addProductController.isLoaded()) addProductController.onLoadTask();
                addProductController.show();
            }
        });
    }

    @Override
    public void onLoadTask() {
        super.onLoadTask();
        addProductController = new AddProductController(this);
        Loader loader = Loader.getInstance();
        loader.load("fxml/add_product.fxml", addProductController);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                ArrayList<Product> products = database.getAllProducts();
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (products != null) {
                        itemsTable.setItems(FXCollections.observableArrayList(products));
                    }
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    ErrorDialog.show(ex.getErrorCode() + "", ex.getLocalizedMessage());
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
}
