package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.EnumBillingType;
import java.util.Date;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author gemini1991
 */
public final class BillingsTableHelper {

    public static void setupTable(final TableView<Billing> table) {
        if (table == null) throw new IllegalArgumentException("TableView must not be null!");
        
        final TableColumn<Billing, Date> colDateOfBilling = new TableColumn<>("Date of Billing");
        final TableColumn<Billing, String> colAccountNo = new TableColumn<>("Account No");
        final TableColumn<Billing, Account> colName = new TableColumn<>("Name");
        final TableColumn<Billing, String> colDueDate = new TableColumn<>("Due Date");
        final TableColumn<Billing, Double> colAmount = new TableColumn<>("Amount");
        final TableColumn<Billing, String> colStatus = new TableColumn<>("Status");
        final TableColumn<Billing, EnumBillingType> colType = new TableColumn<>("Type");
    
        colDateOfBilling.setCellValueFactory(new PropertyValueFactory<>("billingDate"));
        colDateOfBilling.setCellFactory(new BillingDateTableCellFactory());
        
        colAccountNo.setCellValueFactory(new PropertyValueFactory<>("accountNo"));
        
        colName.setCellValueFactory(new PropertyValueFactory<>("account"));
        colName.setCellFactory(new BillingNameTableCellFactory());
        
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colDueDate.setCellFactory(new BillingDateTableCellFactory2());
        
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.getStyleClass().add("table-cell-right-align");
        
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.getStyleClass().add("table-cell-center-align");
        
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.getStyleClass().add("table-cell-center-align");
        
        table.getColumns().addAll(
                colStatus, colDueDate, colAmount, colAccountNo, colName, colDateOfBilling, colType
        );
        
        table.setRowFactory(row -> new BillingTableRow());
    }  
    
}
