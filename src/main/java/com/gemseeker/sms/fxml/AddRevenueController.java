package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.AbstractFxmlWindowController;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumBillingType;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.data.Revenue;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class AddRevenueController extends AbstractFxmlWindowController {
    
    @FXML private TextField tfAmount;
    @FXML private ChoiceBox<String> cbTypes;
    @FXML private DatePicker datePicker;
    @FXML private TextArea taDescription;
    @FXML private Button btnAdd;
    @FXML private Button btnCancel;
    @FXML private ProgressBar progressBar;
    
    private final SalesController salesController;
    private final CompositeDisposable disposables;
    
    public AddRevenueController(SalesController salesController) {
        super(AddRevenueController.class.getResource("add_revenue.fxml"));
        this.salesController = salesController;
        disposables = new CompositeDisposable();
    }

    @Override
    public void openWindow() {
        // Call super.openWindow() first.
        // This is to make sure that the components (ie. stage, scene and root)
        // are created first.
        super.openWindow();
        clearFields();
    }
    
    private void clearFields() {
        tfAmount.clear();
        cbTypes.getSelectionModel().select(0);
        datePicker.setValue(LocalDate.now());
        taDescription.clear();
    }
    
    private boolean fieldsValidated() {
        return !tfAmount.getText().isEmpty() &&
                cbTypes.getSelectionModel().getSelectedIndex() > -1 &&
                datePicker.getValue() != null;
    }
    
    private void save() {
        Revenue revenue = getRevenueInfo();
        showProgress();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            boolean added = database.addRevenue(revenue);
            if (added) {
                // add to history
                History history = new History();
                history.setDate(Utils.getDateNow());
                history.setTitle("New Revenue Entry");
                history.setDescription("Added new revenue entry from " + revenue.getType() + " with the amount of " + revenue.getAmount());
                database.addHistory(history);
            }
            return added;
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(added -> {
                    hideProgress();
                    if (!added) {
                        ErrorDialog.show("Oh-snap!", "Failed to add revenue entry. Try again.");
                    } else {
                        closeWindow();
                        salesController.refresh();
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        hideProgress();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private Revenue getRevenueInfo() {
        Revenue revenue = new Revenue();
        revenue.setAmount(Double.parseDouble(tfAmount.getText()));
        try {
            String dateStr = datePicker.getEditor().getText();
            Date date = Utils.DATE_FORMAT_2.parse(dateStr);
            revenue.setDate(date);
        } catch (ParseException e) {
            System.err.println("Failed to parse date.\n" + e);
            revenue.setDate(Utils.getDateNow());
        }
        revenue.setType(cbTypes.getValue());
        revenue.setDescription(taDescription.getText());
        return revenue;
    }

    @Override
    protected void onFxmlLoaded() {
        Utils.setAsNumericalTextField(tfAmount);
        cbTypes.setItems(FXCollections.observableArrayList(
                EnumBillingType.WISP.getName(),
                EnumBillingType.ITEM.getName(),
                "Other"
        ));
        
        btnAdd.setOnAction(evt -> {
            if (fieldsValidated()) {
                save();
            }
        });
        
        btnCancel.setOnAction(evt -> {
            closeWindow();
        });
    }

    @Override
    public void onCloseRequest(WindowEvent windowEvent) {
        disposables.dispose();
    }

    private void showProgress() {
        progressBar.setVisible(true);
    }
    
    private void hideProgress() {
        progressBar.setVisible(false);
    }
}
