package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.AbstractFxmlWindowController;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.util.Calendar;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class ChangeDueDateController extends AbstractFxmlWindowController {

    @FXML private TextField tfCurrentDueDate;
    @FXML private DatePicker dpDueDate;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    
    private final BillingsController billingsController;
    private final CompositeDisposable disposables;
    private Billing billing;
    
    public ChangeDueDateController(BillingsController billingsController) {
        super(ChangeDueDateController.class.getResource("change_due_date.fxml"));
        this.billingsController = billingsController;
        disposables = new CompositeDisposable();
    }
    
    @Override
    public void onFxmlLoaded() {
        btnCancel.setOnAction(evt -> closeWindow());
        btnConfirm.setOnAction(evt -> {
            save();
            closeWindow();
        });
    }

    @Override
    public void onCloseRequest(WindowEvent windowEvent) {
        disposables.dispose();
    }
    
    public void openWindow(Billing billing) {
        openWindow(); // AbstractFxmlWindowController
        clearFields();
        this.billing = billing;
        tfCurrentDueDate.setText(billing.getDueDate());
    }

    private void clearFields() {
        tfCurrentDueDate.clear();
        dpDueDate.getEditor().clear();
    }
    
    private void save() {
        String dateStr = dpDueDate.getEditor().getText();
        if (!dateStr.isEmpty() && billing != null) {
            ProgressBarDialog.show();
            disposables.add(Observable.fromCallable(() -> {
                Database database = Database.getInstance();
                boolean updated = database.updateBilling(billing.getBillingId(), "due_date", dateStr);
                if (updated) {
                    database.updateBilling(billing.getBillingId(), "date_updated",
                            Utils.MYSQL_DATETIME_FORMAT.format(Calendar.getInstance().getTime()));

                    // add to history
                    History history = new History();
                    history.setTitle("Update Billing");
                    history.setDescription(String.format("Updated billing with ID %d. Changed due date from %s to %s",
                            billing.getBillingId(), billing.getDueDate(), dateStr));
                    history.setDate(Utils.getDateNow());
                    database.addHistory(history);
                }
                return updated;
            }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                    .subscribe(updated -> {
                        ProgressBarDialog.close();
                        if (!updated) ErrorDialog.show("Oh Snap!", "Error while updating billing entry.");
                        else billingsController.updateBillingTable();
                    }, err -> {
                        if (err.getCause() != null) {
                            ProgressBarDialog.close();
                            ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                        }
                    }));
        }
    }
}
