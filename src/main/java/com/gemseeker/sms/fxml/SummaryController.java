package com.gemseeker.sms.fxml;

import com.gemseeker.sms.core.AbstractPanelController;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumAccountStatus;
import com.gemseeker.sms.data.EnumBillingStatus;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

/**
 *
 * @author gemini1991
 */
public class SummaryController extends AbstractPanelController {
    
    @FXML private Label lblAccountsCount;
    @FXML private Label lblActiveCount;
    @FXML private Label lblDisconnectedCount;
    @FXML private Label lblTerminatedCount;
    
    @FXML private Label lblBillingsCount;
    @FXML private Label lblForReviewCount;
    @FXML private Label lblForPaymentCount;
    @FXML private Label lblPaidCount;
    @FXML private Label lblOverdueCount;
    
    private final CompositeDisposable disposables;
    
    public SummaryController() {
        disposables = new CompositeDisposable();
    }

    @Override
    protected void makePanel() {
        final URL fxmlURL = SummaryController.class.getResource("summary.fxml");
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
        
        // TODO : init components and listeners
    }

    public void refresh() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
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
            
            return Arrays.asList(
                    accountsCount,      // 0
                    activeCount,        // 1
                    disconnectedCount,  // 2
                    terminatedCount,    // 3
                    billingsCount,      // 4
                    forReviewCount,     // 5
                    forPaymentCount,    // 6
                    paidCount,          // 7
                    overdueCount        // 8
            );
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(counts -> {
                    ProgressBarDialog.close();
                    lblAccountsCount.setText(counts.get(0) + "");
                    lblActiveCount.setText(counts.get(1) + "");
                    lblDisconnectedCount.setText(counts.get(2) + "");
                    lblTerminatedCount.setText(counts.get(3) + "");
                    
                    lblBillingsCount.setText(counts.get(4) + "");
                    lblForReviewCount.setText(counts.get(5) + "");
                    lblForPaymentCount.setText(counts.get(6) + "");
                    lblPaidCount.setText(counts.get(7) + "");
                    lblOverdueCount.setText(counts.get(8) + "");
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
}
