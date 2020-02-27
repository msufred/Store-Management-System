package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Product;
import com.gemseeker.sms.data.Service;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author gemini1991
 */
public class ServicesTable extends TableView<Service> {

    private TableColumn<Service, String> colName;
    private TableColumn<Service, Double> colPrice;
    private TableColumn<Service, String> colDesc;
    
    public ServicesTable() {
        init();
    }
    
    private void init() {
        colName = new TableColumn<>("Name");
        colName.setPrefWidth(150);
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        colPrice = new TableColumn<>("Est. Price");
        colPrice.setPrefWidth(150);
        colPrice.getStyleClass().add("table-cell-center-align");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("estPrice"));
        
        colDesc = new TableColumn<>("Description");
        colDesc.setPrefWidth(300);
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        getColumns().addAll(colName, colPrice, colDesc);

//        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }
}
