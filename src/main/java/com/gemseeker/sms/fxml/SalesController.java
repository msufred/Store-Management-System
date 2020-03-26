package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Loader;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingType;
import com.gemseeker.sms.data.Expense;
import com.gemseeker.sms.data.Revenue;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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
    @FXML private Label lblWispPercentage;
    @FXML private Label lblProductsPercentage;
    @FXML private Label lblOthersPercentage;
    @FXML private Label lblTotalPercentage;
    @FXML private Label lblExpensesPercentage;
    @FXML private LineChart<String, Double> salesLineChart;
    @FXML private PieChart pieChart;
    @FXML private PieChart expensesOverRevenuesChart;
    @FXML private TableView<Revenue> recentRevenuesTable;
    @FXML private TableColumn<Revenue, Integer> colRevenueNo;
    @FXML private TableColumn<Revenue, Double> colRevenueAmount;
    @FXML private TableColumn<Revenue, String> colRevenueType;
    @FXML private TableColumn<Revenue, Date> colRevenueDate;
    @FXML private TableView<Expense> recentExpensesTable;
    @FXML private TableColumn<Expense, Integer> colExpenseNo;
    @FXML private TableColumn<Expense, Double> colExpenseAmount;
    @FXML private TableColumn<Expense, String> colExpenseDescription;
    @FXML private TableColumn<Expense, Date> colExpenseDate;
    
    private AddRevenueController addRevenueController;
    private AddExpenseController addExpenseController;
    
    private final CompositeDisposable disposables;
    
    public SalesController() {
        disposables = new CompositeDisposable();
    }

    @Override
    public void onLoadTask() {
        super.onLoadTask();
        addRevenueController = new AddRevenueController(this);
        addExpenseController = new AddExpenseController(this);
        
        Loader loader = Loader.getInstance();
        loader.load("fxml/add_revenue.fxml", addRevenueController);
        loader.load("fxml/add_expense.fxml", addExpenseController);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnAddRevenue.setOnAction(evt -> {
            if (addRevenueController != null) {
                if (!addRevenueController.isLoaded()) addRevenueController.onLoadTask();
                addRevenueController.show();
            }
        });
        
        btnAddExpense.setOnAction(evt -> {
            if (addExpenseController != null) {
                if (!addExpenseController.isLoaded()) addExpenseController.onLoadTask();
                addExpenseController.show();
            }
        });
        
        colRevenueNo.setCellValueFactory(new PropertyValueFactory<>("revenueNo"));
        colRevenueNo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setText("# " + item);
                }
            }
        });
        colRevenueAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colRevenueType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRevenueDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colRevenueDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (!empty && date != null) {
                    setText(Utils.TABLE_DATE_FORMAT.format(date));
                } else {
                    setText(null);
                }
            }
        });
        
        colExpenseNo.setCellValueFactory(new PropertyValueFactory<>("expenseNo"));
        colExpenseNo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText("# " + item);
                } else {
                    setText(null);
                }
            }
        });
        colExpenseAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colExpenseDescription.setCellValueFactory(new PropertyValueFactory<>("type"));
        colExpenseDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colExpenseDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (!empty && date != null) {
                    setText(Utils.TABLE_DATE_FORMAT.format(date));
                }
            }
        });
    } 

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
    
    public void refresh() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            ArrayList<Revenue> revenues = database.getAllRevenues();
            ArrayList<Expense> expenses = database.getAllExpenses();
            return Arrays.asList(revenues, expenses);
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(entries -> {
                    ProgressBarDialog.close();
                    showDetails((ArrayList<Revenue>)entries.get(0), (ArrayList<Expense>)entries.get(1));
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }

    /**
     * Displays the details and charts of revenues and expenses this month. Also
     * displays the percentage of revenues and expenses over the past month.
     * 
     * @param revenues
     * @param expenses 
     */
    private void showDetails(ArrayList<Revenue> revenues, ArrayList<Expense> expenses) {
        // revenues this month
        double totalRevenue = 0;
        double wispRevenue = 0;
        double productsRevenue = 0;
        double othersRevenue = 0;
        
        // recenues past month
        double pastTotalRevenue = 0;
        double pastWispRevenue = 0;
        double pastProductsRevenue = 0;
        double pastOthersRevenue = 0;
        
        // get Calendar today
        Calendar calNow = Calendar.getInstance();
        calNow.setTime(Utils.getDateNow());
        
        for (Revenue r : revenues){
            Calendar cal = Calendar.getInstance();
            cal.setTime(r.getDate());

            // current month
            if (cal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH)) {
                totalRevenue += r.getAmount();
                if (r.getType().equals(EnumBillingType.WISP.getName())) {
                    wispRevenue += r.getAmount();
                } else if (r.getType().equals(EnumBillingType.ITEM.getName())) {
                    productsRevenue += r.getAmount();
                } else {
                    othersRevenue += r.getAmount();
                }
            }

            // past month
            if (cal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH) - 1) {
                pastTotalRevenue += r.getAmount();
                if (r.getType().equals(EnumBillingType.WISP.getName())) {
                    pastWispRevenue += r.getAmount();
                } else if (r.getType().equals(EnumBillingType.ITEM.getName())) {
                    pastProductsRevenue += r.getAmount();
                } else {
                    pastOthersRevenue += r.getAmount();
                }
            }
        }
        
        // display amounts
        lblWISPCount.setText(Utils.toStringMoneyFormat(wispRevenue));
        lblProductsCount.setText(Utils.toStringMoneyFormat(productsRevenue));
        lblOthersCount.setText(Utils.toStringMoneyFormat(othersRevenue));
        lblTotalSalesCount.setText(Utils.toStringMoneyFormat(totalRevenue));
        
        // display percentages
        double wispPercentage = pastWispRevenue == 0 ? 100 : Math.round((wispRevenue / pastWispRevenue) * 100);
        lblWispPercentage.setText(String.format("%.0f", wispPercentage) + " %");
        double productsPercentage = pastProductsRevenue == 0 ? 100 : Math.round((productsRevenue / pastProductsRevenue) * 100);
        lblProductsPercentage.setText(String.format("%.0f", productsPercentage) + "%");
        double othersPercentage = pastOthersRevenue == 0 ? 100 : Math.round((othersRevenue / pastOthersRevenue) * 100);
        lblOthersPercentage.setText(String.format("%.0f", othersPercentage) + "%");
        double totalPercentage = pastTotalRevenue == 0 ? 100 : Math.round((totalRevenue / pastTotalRevenue) * 100);
        lblTotalPercentage.setText(String.format("%.0f", totalPercentage) + "%");
        
        double totalExpenses = 0; // expenses this month
        double pastTotalExpenses = 0; // expenses past month
        for (Expense e : expenses) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(e.getDate());
            if (cal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH)) {
                totalExpenses += e.getAmount();
            }
            
            if (cal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH) - 1) {
                pastTotalExpenses += e.getAmount();
            }
        }
        
        // display expenses
        lblExpensesCount.setText(Utils.toStringMoneyFormat(totalExpenses));
        
        // display expenses percentage
        double expensesPercentage = pastTotalExpenses == 0 ? 100 : Math.round((totalExpenses / pastTotalExpenses) * 100);
        lblExpensesPercentage.setText(String.format("%.0f", expensesPercentage) + "%");
        
        salesLineChart.getData().clear();
        
        // double array that corresponds to the number of days per week
        double[] daysRevenuesArr = new double[7];
        
        // set all values in array to zero
        for (int i = 0; i<daysRevenuesArr.length; i++) daysRevenuesArr[i] = 0;
        
        for (Revenue r : revenues) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(r.getDate());
            if (cal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH) &&
                    cal.get(Calendar.WEEK_OF_MONTH) == calNow.get(Calendar.WEEK_OF_MONTH)) {
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek - 1 < daysRevenuesArr.length) {
                    daysRevenuesArr[dayOfWeek - 1] += r.getAmount();
                }
            }
        }
        
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        ObservableList<XYChart.Data<String, Double>> dataList = FXCollections.observableArrayList();
        for (int i = 0; i < daysRevenuesArr.length; i++) {
            XYChart.Data<String, Double> data = new XYChart.Data<>();
            switch (i) {
                case 1:
                    data.setXValue("Mon");
                    break;
                case 2:
                    data.setXValue("Tue");
                    break;
                case 3:
                    data.setXValue("Wed");
                    break;
                case 4:
                    data.setXValue("Thu");
                    break;
                case 5:
                    data.setXValue("Fri");
                    break;
                case 6:
                    data.setXValue("Sat");
                    break;
                default:
                    data.setXValue("Sun");
                    break;
            }
            data.setYValue(daysRevenuesArr[i]);
            dataList.add(data);
        }
        series.setName("Sales/Revenues This Week");
        series.setData(dataList);
        salesLineChart.getData().add(series);
        
        // pie chart
        pieChart.getData().clear();
        pieChart.getData().addAll(
                new PieChart.Data("WISP", wispRevenue),
                new PieChart.Data("Products", productsRevenue),
                new PieChart.Data("Others", othersRevenue)
        );
        
        expensesOverRevenuesChart.getData().clear();
        expensesOverRevenuesChart.getData().addAll(
                new PieChart.Data("Expenses", totalExpenses),
                new PieChart.Data("Revenues", totalRevenue)
        );
        
        // show recent revenue collections
        Collections.sort(revenues, (r1, r2) -> {
            return (int)(r2.getDate().getTime() - r1.getDate().getTime());
        });
        if (revenues.size() <= 10) {
            recentRevenuesTable.setItems(FXCollections.observableArrayList(revenues));
        } else {
            ArrayList<Revenue> recent = new ArrayList<>();
            for (Revenue r : revenues) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(r.getDate());
                if (cal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH)) {
                    int dayOfMonth = calNow.get(Calendar.DAY_OF_MONTH);
                    int min = (dayOfMonth - 10 < 1) ? 1 : dayOfMonth - 10;
                    for (int i = dayOfMonth; i >= min; i--){
                        if (cal.get(Calendar.DAY_OF_MONTH) == i) recent.add(r);
                    }
                }
            }
            recentRevenuesTable.setItems(FXCollections.observableArrayList(recent));
        }
        
        // show recent expenses
        Collections.sort(expenses, (e1, e2) -> {
            return (int)(e2.getDate().getTime() - e1.getDate().getTime());
        });
        if (expenses.size() <= 10) {
            recentExpensesTable.setItems(FXCollections.observableList(expenses));
        } else {
            
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }
}
