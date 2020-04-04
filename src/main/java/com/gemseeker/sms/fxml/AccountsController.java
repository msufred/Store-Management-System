package com.gemseeker.sms.fxml;

import com.gemseeker.seekiconsfx.material.AddIcon6;
import com.gemseeker.seekiconsfx.material.CreateIcon;
import com.gemseeker.seekiconsfx.material.DeleteIcon;
import com.gemseeker.seekiconsfx.material.RefreshIcon;
import com.gemseeker.seekiconsfx.material.SearchIcon;
import com.gemseeker.sms.core.AbstractPanelController;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Address;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumAccountStatus;
import com.gemseeker.sms.data.InternetSubscription;
import com.gemseeker.sms.fxml.components.AccountNameTableCell;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.InfoDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 *
 * @author gemini1991
 */
public class AccountsController extends AbstractPanelController {

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Button btnChangeStatus;
    @FXML private Button btnSearch;
    
    @FXML private TableView<Account> accountsTable;
    @FXML private TableColumn<Account, String> colAccountNo;
    @FXML private TableColumn<Account, String> colName;
    @FXML private TableColumn<Account, Address> colAddress;
    @FXML private TableColumn<Account, String> colContact;
    @FXML private TableColumn<Account, EnumAccountStatus> colStatus;
    @FXML private Label labelEmpty;
    @FXML private VBox detailsContent;
    
    // account details group
    @FXML private TitledPane detailsGroupPane;
    @FXML private Label lblAccountNo;
    @FXML private Label lblName;
    @FXML private Label lblAddress;
    @FXML private Label lblContact;
    @FXML private Label lblStatus;
    @FXML private Label lblType;
    
    // subscription group
    @FXML private TitledPane internetGroupPane;
    @FXML private Label lblBandwidth;
    @FXML private Label lblAmount;
    @FXML private Label lblIPAddress;
    @FXML private Label lblLongitude;
    @FXML private Label lblLatitude;
    @FXML private Label lblElevation;
    
    // icons
    private static final double ICON_SIZE = 12;
    private final AddIcon6 addIcon;
    private final CreateIcon editIcon;
    private final DeleteIcon deleteIcon;
    private final RefreshIcon refreshIcon;
    private final SearchIcon searchIcon;
    
    private final AddAccountController addAccountController = new AddAccountController(this);
    private final CompositeDisposable disposables;
    
    public AccountsController() {
        disposables = new CompositeDisposable();
        
        // create icons
        addIcon = new AddIcon6(ICON_SIZE);
        editIcon = new CreateIcon(ICON_SIZE);
        deleteIcon = new DeleteIcon(ICON_SIZE);
        refreshIcon = new RefreshIcon(ICON_SIZE + 4);
        searchIcon = new SearchIcon(ICON_SIZE);
    }

    @Override
    protected void makePanel() {
        final URL fxmlURL = AccountsController.class.getResource("accounts.fxml");
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
        
        // Init components and listeners
        colAccountNo.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colName.setCellFactory(col -> new AccountNameTableCell());
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
//        accountsTable.setRowFactory(row -> new AccountTableRow());
        accountsTable.getSelectionModel().selectedItemProperty().addListener((o, a1, a2) -> {
            detailsContent.setVisible(a2 != null);
            labelEmpty.setVisible(a2 == null);
            disableActions(a2 == null);
            if (a2 != null) {
                showAccountDetails(a2);
                if (!detailsGroupPane.isExpanded() && !internetGroupPane.isExpanded()) {
                    detailsGroupPane.setExpanded(true);
                }
            }
        });
        
        btnAdd.setGraphic(addIcon);
        btnAdd.setOnAction(evt -> {
            if (addAccountController != null) addAccountController.openWindow();
        });
        
        btnEdit.setGraphic(editIcon);
        btnEdit.setOnAction(evt -> {
            InfoDialog.show("Info Sample", "This is a sample");
        });
        
        btnDelete.setGraphic(deleteIcon);
        btnDelete.setOnAction(evt -> {
            Account account = accountsTable.getSelectionModel().getSelectedItem();
            if(account != null) {
                Alert alert = new Alert(AlertType.WARNING, null, ButtonType.CANCEL, ButtonType.YES);
                alert.setTitle("Delete Account?");
                alert.setHeaderText("Are you sure you want to delete this account?");
                alert.setContentText("Deleting this account will also delete billing entries "
                        + "and other data related to this account!");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.isPresent()) {
                    if (option.get() == ButtonType.YES) {
                        deleteAccount(account);
                    } else {
                        alert.close();
                    }
                }
            }
        });
        
        btnRefresh.setGraphic(refreshIcon);
        btnRefresh.setOnAction(evt -> refresh());
        
        btnSearch.setGraphic(searchIcon);
    }

    @Override
    public void dispose() {
        super.dispose();
        disposables.dispose();
    }

    public void refresh() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            return Database.getInstance().getAllAccounts();
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(accts -> {
                    ProgressBarDialog.close();
                    accountsTable.setItems(FXCollections.observableArrayList(accts));
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private void disableActions(boolean disable) {
        btnEdit.setDisable(disable);
        btnDelete.setDisable(disable);
        btnChangeStatus.setDisable(disable);
    }
    
    private void showAccountDetails(Account account) {
        lblAccountNo.setText(account.getAccountNumber());
        lblName.setText(String.format("%s %s", account.getFirstName(), account.getLastName()));
        Address address = account.getAddress();
        if (address != null) {
            lblAddress.setText(address.toString());
        } else {
            lblAddress.setText("<Not Set>");
        }
        lblContact.setText(account.getContactNumber());
        lblStatus.setText(account.getStatus().toString());
        lblType.setText(account.getAccountType().toString());
        
        // Internet Subscription
        InternetSubscription sub = account.getInternetSubscription();
        if (sub != null) {
            int bandwidth = sub.getBandwidth();
            if (bandwidth > 0) {
                lblBandwidth.setText(sub.getBandwidth() + "");
            } else {
                lblBandwidth.setText("Unlimited");
            }
            lblAmount.setText(sub.getAmount() + "");
            lblIPAddress.setText(sub.getIpAddress());
            lblLatitude.setText(sub.getLatitude() + "");
            lblLongitude.setText(sub.getLongitude() + "");
            lblElevation.setText(sub.getElevation() + "");
        } else {
            lblBandwidth.setText("<Not Set>");
            lblAmount.setText("<Not Set>");
            lblIPAddress.setText("<Not Set>");
            lblLatitude.setText("<Not Set>");
            lblLongitude.setText("<Not Set>");
            lblElevation.setText("<Not Set>");
        }
    }

    private void deleteAccount(Account account) {
        if (account == null) {
            ErrorDialog.show("Oh snap!", "No selected account.");
            return;
        }
        
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            return Database.getInstance().deleteAccount(account);
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(deleted -> {
                    ProgressBarDialog.close();
                    if (deleted) {
                        refresh();
                    } else {
                        ErrorDialog.show("Account Delete Error", "Failed to delete account.");
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Account Delete Error", err.getLocalizedMessage());
                    }
                }));
    }
}
