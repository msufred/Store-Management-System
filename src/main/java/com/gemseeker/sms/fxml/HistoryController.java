package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.fxml.components.BillingDateTableCell;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.HistoryDateTableCellFactory;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author gemini1991
 */
public class HistoryController extends Controller {

    @FXML private TableView<History> historyTable;
    @FXML private TableColumn<History, Date> colDate;
    @FXML private TableColumn<History, String> colTitle;
    @FXML private TableColumn<History, String> colDescription;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(new HistoryDateTableCellFactory());
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    @Override
    public void onLoadTask() {
        super.onLoadTask();
    }

    @Override
    public void onResume() {
        super.onResume(); 
        refresh();
    }

    public void refresh() {
        ProgressBarDialog.show();
        Thread t = new Thread(() -> {
            try {
                Database database = Database.getInstance();
                ArrayList<History> histories = database.getHistories();
                Platform.runLater(() -> {
                    ProgressBarDialog.close();
                    historyTable.setItems(FXCollections.observableArrayList(histories));
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
