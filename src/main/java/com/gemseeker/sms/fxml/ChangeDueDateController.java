package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class ChangeDueDateController extends Controller {

    @FXML private TextField tfCurrentDueDate;
    @FXML private DatePicker dpDueDate;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    
    private Stage stage;
    private Scene scene;
    
    private final BillingsController billingsController;
    private Billing billing;
    
    public ChangeDueDateController(BillingsController billingsController) {
        this.billingsController = billingsController;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnCancel.setOnAction(evt -> close());
        btnConfirm.setOnAction(evt -> {
            save();
            close();
        });
    }

    public void show(Billing billing) {
        clearFields();
        if (stage == null) {
            stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Change Due Date");
            scene = new Scene(getContentPane());
            stage.setScene(scene);
        }
        stage.show();
        this.billing = billing;
        tfCurrentDueDate.setText(billing.getDueDate());
    }
    
    public void close() {
        if (stage != null) stage.close();
    }
    
    private void clearFields() {
        tfCurrentDueDate.clear();
        dpDueDate.getEditor().clear();
    }
    
    private void save() {
        String dateStr = dpDueDate.getEditor().getText();
        if (!dateStr.isEmpty() && billing != null) {
            ProgressBarDialog.show();
            Thread t = new Thread(() -> {
                try {
                    Database database = Database.getInstance();
                    boolean updated = database.updateBilling(billing.getBillingId(), "due_date", dateStr);
                    if (updated) {
                        database.updateBilling(billing.getBillingId(), "date_updated",
                                Utils.MYSQL_DATETIME_FORMAT.format(Calendar.getInstance().getTime()));
                    }
                    Platform.runLater(() -> {
                        ProgressBarDialog.close();
                        if (!updated) ErrorDialog.show("Oh Snap!", "Error while updating billing entry.");
                        else billingsController.updateBillingTable();
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
}
