package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingType;
import com.gemseeker.sms.data.Expense;
import com.gemseeker.sms.data.Revenue;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 *
 * @author gemini1991
 */
public class SalesController extends Controller {
    
    @FXML private Button btnAddRevenue;
    @FXML private Button btnAddExpense;
    @FXML private Label lblWISPCount;
    @FXML private Label lblProductsCount;
    @FXML private Label lblOthersCount;
    @FXML private Label lblTotalSalesCount;
    @FXML private Label lblExpensesCount;
    
    @FXML private PieChart pieChart;

    @Override
    public void onLoadTask() {
        super.onLoadTask();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnAddRevenue.setOnAction(evt -> {
        
        });
        
        btnAddExpense.setOnAction(evt -> {
            
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
    
    private void refresh() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                
                ArrayList<Revenue> revenues = database.getAllRevenues();
                ArrayList<Expense> expenses = database.getAllExpenses();

                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    
                    double totalRevenue = 0; 
                    double wispRevenue = 0;
                    double productsRevenue = 0;
                    double othersRevenue = 0;
                    
                    if (!revenues.isEmpty()) {
                        for (Revenue r : revenues){
                            totalRevenue += r.getAmount();
                            if (r.getType().equals(EnumBillingType.WISP.getName())) {
                                wispRevenue += r.getAmount();
                            } else if (r.getType().equals(EnumBillingType.ITEM.getName())) {
                                productsRevenue += r.getAmount();
                            } else {
                                othersRevenue += r.getAmount();
                            }
                        }
                    }
                    
                    lblWISPCount.setText(String.format("%.2f", wispRevenue));
                    lblProductsCount.setText(String.format("%.2f", productsRevenue));
                    lblOthersCount.setText(String.format("%.2f", othersRevenue));
                    lblTotalSalesCount.setText(String.format("%.2f", totalRevenue));

                    // pie chart
                    pieChart.getData().clear();
                    pieChart.getData().addAll(
                            new PieChart.Data("WISP", wispRevenue),
                            new PieChart.Data("Products", productsRevenue),
                            new PieChart.Data("Others", othersRevenue)
                    );
                    
                    if (!expenses.isEmpty()) {
                        double totalExpenses = 0;
                        for (Expense e : expenses) totalExpenses += e.getAmount();
                        lblExpensesCount.setText(String.format("%.2f", totalExpenses));
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
