package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.Expense;
import com.gemseeker.sms.data.Revenue;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class AddExpenseController extends Controller {
    
    @FXML private TextField tfAmount;
    @FXML private ChoiceBox<String> cbTypes;
    @FXML private DatePicker datePicker;
    @FXML private TextArea taDescription;
    @FXML private Button btnAdd;
    @FXML private Button btnCancel;
    
    private Stage stage;
    private Scene scene;
    
    private final SalesController salesController;
    
    public AddExpenseController(SalesController salesController) {
        this.salesController = salesController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utils.setAsNumericalTextField(tfAmount);
        cbTypes.setItems(FXCollections.observableArrayList(
                "Electric Bill", "Water Bill", "Grocery", "Transportation",
                "Other"
        ));
        
        btnAdd.setOnAction(evt -> {
            if (fieldsValidated()) {
                save();
            }
        });
        
        btnCancel.setOnAction(evt -> {
            close();
        });
    }

    public void show() {
        clearFields();
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Add Revenue");
            stage.initModality(Modality.APPLICATION_MODAL);
            scene = new Scene(getContentPane());
            stage.setScene(scene);
        }
        stage.show();
    }
    
    public void close() {
        if (stage != null) stage.close();
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
        Expense expense = getExpenseInfo();
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                boolean added = database.addExpense(expense);
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    if (!added) {
                        ErrorDialog.show("Oh-snap!", "Failed to add expense entry. Try again.");
                    } else {
                        close();
                        salesController.refresh();
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
    
    private Expense getExpenseInfo() {
        Expense expense = new Expense();
        expense.setAmount(Double.parseDouble(tfAmount.getText()));
        try {
            String dateStr = datePicker.getEditor().getText();
            Date date = Utils.DATE_FORMAT_2.parse(dateStr);
            expense.setDate(date);
        } catch (ParseException e) {
            System.err.println("Failed to parse date.\n" + e);
            expense.setDate(Utils.getDateNow());
        }
        expense.setType(cbTypes.getValue());
        expense.setDescription(taDescription.getText());
        return expense;
    }
}
