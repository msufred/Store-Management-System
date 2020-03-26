package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Payment;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author gemini1991
 */
public final class PaymentsTableHelper {

    public static void setupTable(final TableView<Payment> table) {
        final TableColumn<Payment, String> colDescription = new TableColumn<>("Description");
        final TableColumn<Payment, Double> colCost = new TableColumn<>("Amount");
        final TableColumn<Payment, Integer> colQuantity = new TableColumn<>("Quantity");
        final TableColumn<Payment, Double> colTotal = new TableColumn<>("Total Cost");
        
        colDescription.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        table.getColumns().addAll(colDescription, colCost, colQuantity, colTotal);
    }
    
}
