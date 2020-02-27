package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Product;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author gemini1991
 */
public class ProductsTable extends TableView<Product> {

    private TableColumn<Product, String> colName;
    private TableColumn<Product, Double> colPrice;
    private TableColumn<Product, Integer> colCount;
    private TableColumn<Product, Integer> colDescription;
    
    public ProductsTable() {
        init();
    }
    
    private void init() {
        colName = new TableColumn<>("Name");
        colName.setPrefWidth(150);
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        colPrice = new TableColumn<>("Price");
        colPrice.setPrefWidth(150);
        colPrice.getStyleClass().add("table-cell-center-align");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        colCount = new TableColumn<>("In Stock");
        colCount.setPrefWidth(150);
        colCount.getStyleClass().add("table-cell-center-align");
        colCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        
        colDescription = new TableColumn<>("Description");
        colDescription.setPrefWidth(300);
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        getColumns().addAll(colName, colPrice, colCount, colDescription);
        
//        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }
}
