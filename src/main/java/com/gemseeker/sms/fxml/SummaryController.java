package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumAccountStatus;
import com.gemseeker.sms.data.EnumBillingStatus;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

/**
 *
 * @author gemini1991
 */
public class SummaryController extends Controller {
    
    @FXML private Label lblAccountsCount;
    @FXML private Label lblActiveCount;
    @FXML private Label lblDisconnectedCount;
    @FXML private Label lblTerminatedCount;
    
    @FXML private Label lblBillingsCount;
    @FXML private Label lblForReviewCount;
    @FXML private Label lblForPaymentCount;
    @FXML private Label lblPaidCount;
    @FXML private Label lblOverdueCount;
    
    @FXML private PieChart accountsPieChart;
    @FXML private PieChart billingsPieChart;

    @Override
    public void onLoadTask() {
        super.onLoadTask();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
                int accountsCount = database.getAccountsCount();
                int activeCount = database.getAccountsCountByStatus(EnumAccountStatus.ACTIVE);
                int disconnectedCount = database.getAccountsCountByStatus(EnumAccountStatus.DISCONNECTED);
                int terminatedCount = database.getAccountsCountByStatus(EnumAccountStatus.TERMINATED);
                
                int billingsCount = database.getBillingsCount();
                int forReviewCount = database.getBillingsCountByStatus(EnumBillingStatus.FOR_REVIEW);
                int forPaymentCount = database.getBillingsCountByStatus(EnumBillingStatus.FOR_PAYMENT);
                int paidCount = database.getBillingsCountByStatus(EnumBillingStatus.PAID);
                int overdueCount = database.getBillingsCountByStatus(EnumBillingStatus.OVERDUE);
                
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    lblAccountsCount.setText(accountsCount + "");
                    lblActiveCount.setText(activeCount + "");
                    lblDisconnectedCount.setText(disconnectedCount + "");
                    lblTerminatedCount.setText(terminatedCount + "");
                    
                    lblBillingsCount.setText(billingsCount + "");
                    lblForReviewCount.setText(forReviewCount + "");
                    lblForPaymentCount.setText(forPaymentCount + "");
                    lblPaidCount.setText(paidCount + "");
                    lblOverdueCount.setText(overdueCount + "");
                    
                    accountsPieChart.getData().clear();
                    accountsPieChart.getData().addAll(
                            new PieChart.Data("Active", activeCount),
                            new PieChart.Data("Disconnected", disconnectedCount),
                            new PieChart.Data("Terminated", terminatedCount)
                    );
                    
                    billingsPieChart.getData().clear();
                    billingsPieChart.getData().addAll(
                            new PieChart.Data("For Review", forReviewCount),
                            new PieChart.Data("For Payment", forPaymentCount),
                            new PieChart.Data("Paid", paidCount),
                            new PieChart.Data("Overdue", overdueCount)
                    );
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
