package com.gemseeker.sms.fxml;

import com.gemseeker.sms.core.AbstractPanelController;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.HistoryDateTableCellFactory;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author gemini1991
 */
public class HistoryController extends AbstractPanelController {

    @FXML private TableView<History> historyTable;
    @FXML private TableColumn<History, Date> colDate;
    @FXML private TableColumn<History, String> colTitle;
    @FXML private TableColumn<History, String> colDescription;
    
    private final CompositeDisposable disposables;
    
    public HistoryController() {
        disposables = new CompositeDisposable();
    }

    public void refresh() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            return Database.getInstance().getHistories();
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(histories -> {
                    ProgressBarDialog.close();
                    historyTable.setItems(FXCollections.observableArrayList(histories));
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }

    @Override
    protected void makePanel() {
        final URL fxmlURL = HistoryController.class.getResource("history.fxml");
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
        // Assert & init components
        assert historyTable != null;
        assert historyTable.getParent() != null;
        
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(new HistoryDateTableCellFactory());
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // refresh table
        refresh();
    }

    @Override
    public void dispose() {
        super.dispose();
        disposables.dispose();
    }
    
}
