package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.AbstractFxmlWindowController;
import com.gemseeker.sms.data.EnumAccountStatus;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Address;
import com.gemseeker.sms.data.Database;
import com.gemseeker.sms.data.EnumAccountType;
import com.gemseeker.sms.data.History;
import com.gemseeker.sms.data.InternetSubscription;
import com.gemseeker.sms.fxml.components.ErrorDialog;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class AddAccountController extends AbstractFxmlWindowController {

    @FXML private RadioButton rbPersonal;
    @FXML private RadioButton rbCommercial;
    @FXML private Button btnGenerate;
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    
    @FXML private TextField tfAccountNo;
    @FXML private TextField tfFirstname;
    @FXML private TextField tfLastname;
    @FXML private TextField tfLandmark;
    @FXML private ComboBox<String> cbProvince;
    @FXML private ComboBox<String> cbCities;
    @FXML private ComboBox<String> cbBrgy;
    @FXML private TextField tfContactNo;

    @FXML private CheckBox cbAddInternet;
    @FXML private HBox bandwidthGroup;
    @FXML private ChoiceBox<String> cbDataPlan;
    @FXML private TextField tfMonthlyPayment;
    
    @FXML private HBox ipGroup;
    @FXML private TextField tfIP01; // ip address
    @FXML private TextField tfIP02; // -do-
    @FXML private TextField tfIP03; // -do-
    @FXML private TextField tfIP04; // -do-
    
    @FXML private HBox latitudeGroup;
    @FXML private TextField tfLatitude;
    
    @FXML private HBox longitudeGroup;
    @FXML private TextField tfLongitude;
    
    @FXML private HBox elevationGroup;
    @FXML private TextField tfElevation;
    
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    private final AccountsController accountsController;
    private final CompositeDisposable disposables;
    
    public AddAccountController(AccountsController accountsController) {
        super(AddAccountController.class.getResource("add_account.fxml"));
        this.accountsController = accountsController;
        disposables = new CompositeDisposable();
    }
    
    @Override
    protected void onFxmlLoaded() {
        final ToggleGroup rbGroup = new ToggleGroup();
        rbGroup.getToggles().addAll(rbPersonal, rbCommercial);
        
        btnGenerate.setOnAction(evt -> generateAccountNo());
        
        cbProvince.setItems(Utils.getProvinceList());
        cbProvince.getSelectionModel().selectedItemProperty().addListener((o, s1, s2) -> {
            if (s2 != null) cbCities.setItems(Utils.getCityList(s2));
        });
        cbCities.getSelectionModel().selectedItemProperty().addListener((o, s1, s2) -> {
            if (s2 != null) cbBrgy.setItems(Utils.getBarangayList(s2));
        });
        
        cbAddInternet.selectedProperty().addListener((ov, s, selected) -> {
            enableInternetGroup(selected);
        });
        
        cbDataPlan.setItems(FXCollections.observableArrayList("5", "10", "100", "Unlimited"));
        cbDataPlan.getSelectionModel().select(0);
        
        tfMonthlyPayment.addEventFilter(KeyEvent.KEY_TYPED, evt -> {
            if (!"01234569.".contains(evt.getCharacter())) evt.consume();
        });
       
        btnSave.setOnAction(evt -> {
            save();
            closeWindow();
            accountsController.refresh();
        });
        btnCancel.setOnAction(evt -> closeWindow());
        
        setupTextFields(tfFirstname, tfLastname, tfLandmark, tfContactNo);
        Utils.setAsNumericalTextFields(tfIP01, tfIP02, tfIP03, tfIP04, tfLatitude, tfLongitude, tfElevation);
    }

    @Override
    public void onCloseRequest(WindowEvent windowEvent) {
        disposables.dispose();
    }

    @Override
    public void openWindow() {
        super.openWindow();
        clearFields();
    }
    
    private void clearFields() {
        rbPersonal.setSelected(true);
        tfAccountNo.clear();
        tfFirstname.clear();
        tfLastname.clear();
        tfLandmark.clear();
        cbProvince.getSelectionModel().select(-1);
        cbCities.getSelectionModel().select(-1);
        cbBrgy.getSelectionModel().select(-1);
        tfContactNo.clear();
        cbAddInternet.setSelected(false);
        cbDataPlan.getSelectionModel().select(-1);
        tfMonthlyPayment.clear();
        tfIP01.clear();
        tfIP02.clear();
        tfIP03.clear();
        tfIP04.clear();
        tfLatitude.clear();
        tfLongitude.clear();
        tfElevation.clear();
    }
    
    private void enableInternetGroup(boolean enable) {
        bandwidthGroup.setDisable(!enable);
        ipGroup.setDisable(!enable);
        latitudeGroup.setDisable(!enable);
        longitudeGroup.setDisable(!enable);
        elevationGroup.setDisable(!enable);
    }
    
    private void generateAccountNo() {
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            int count = database.getAccountsCount();
            if (count == 0) return "ACCT-0";
            else {
                String acctNo;
                while (true) {
                    acctNo = "ACCT-" + count;
                    if (database.hasAccountNo(acctNo)) count++;
                    else break;
                }
                return acctNo;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(acctNo -> {
                    tfAccountNo.setText(acctNo);
                }, err -> {
                    if (err.getCause() != null) {
                        ErrorDialog.show("Uh oh!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private void setupTextFields(TextField...textFields) {
        for (TextField tf: textFields) {
            tf.textProperty().addListener((ov, t, t1) -> {
                validate();
            });
        }
    }
    
    private void validate() {
        btnSave.setDisable(
            tfAccountNo.getText().isEmpty() ||
            tfFirstname.getText().isEmpty() ||
            tfLastname.getText().isEmpty() ||
            tfLandmark.getText().isEmpty() ||
            cbProvince.getValue() == null ||
            cbCities.getValue() == null ||
            cbBrgy.getValue() == null ||
            tfContactNo.getText().isEmpty()
        );
    }
    
    private void save() {
        ProgressBarDialog.show();
        Account account = getAccountInfo();
        disposables.add(Observable.fromCallable(() -> {
            Database database = Database.getInstance();
            boolean added = database.addAccount(account);
            if (added) {
                History history = new History();
                history.setTitle("New Account");
                history.setTitle(String.format("Added new account: [%s] %s %s",
                        account.getAccountNumber(),
                        account.getFirstName(),
                        account.getLastName()));
                history.setDate(Utils.getDateNow());
                database.addHistory(history);
            }
            return added;
        }).subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(added -> {
                    ProgressBarDialog.close();
                    if (!added) {
                        ErrorDialog.show("Oh snap!", "Failed to add account entry to the dabatase.");
                    }
                }, err -> {
                    if (err.getCause() != null) {
                        ProgressBarDialog.close();
                        ErrorDialog.show("Oh snap!", err.getLocalizedMessage());
                    }
                }));
    }
    
    private Account getAccountInfo() {
        Account newAccount = new Account();
        newAccount.setAccountNumber(tfAccountNo.getText());
        String firstName = tfFirstname.getText();
        String lastName = tfLastname.getText();
        newAccount.setAccountUserName(lastName, firstName);
        
        try {
            String startStr = dpStart.getEditor().getText();
            if (!startStr.isEmpty()) {
                Date start = Utils.DATE_FORMAT_2.parse(startStr);
                newAccount.setContractStart(start);
            }
            
            String endStr = dpEnd.getEditor().getText();
            if (!endStr.isEmpty()) {
                Date end = Utils.DATE_FORMAT_2.parse(endStr);
                newAccount.setContractEnd(end);
            }
        } catch (ParseException e) {
            System.err.println("Error parsing date.");
        }
        
        Address address = new Address();
        address.setLandmark(tfLandmark.getText());
        address.setBarangay(cbBrgy.getValue());
        address.setCity(cbCities.getValue());
        address.setProvince(cbProvince.getValue());
        newAccount.setAddress(address);
        
        newAccount.setContactNumber(tfContactNo.getText());
        
        if (cbAddInternet.isSelected() && internetInfoValidated()) {
            InternetSubscription iSub = getInternetInfo();
            newAccount.setInternetSubscription(iSub);
        }
        
        newAccount.setDateRegistered(Calendar.getInstance().getTime());
        newAccount.setStatus(EnumAccountStatus.ACTIVE);
        
        if(rbPersonal.isSelected()) {
            newAccount.setAccountType(EnumAccountType.PERSONAL);
        } else {
            newAccount.setAccountType(EnumAccountType.COMMERCIAL);
        }
        
        return newAccount;
    }
    
    private InternetSubscription getInternetInfo() {
        InternetSubscription internet = new InternetSubscription();
        String dataStr = cbDataPlan.getValue();
        int data = -1;
        if (!dataStr.equalsIgnoreCase("Unlimited")) {
            data = Integer.parseInt(dataStr);
        }
        internet.setBandwidth(data);
        internet.setAmount(Double.parseDouble(tfMonthlyPayment.getText()));
        String ip01 = tfIP01.getText();
        String ip02 = tfIP02.getText();
        String ip03 = tfIP03.getText();
        String ip04 = tfIP04.getText();
        String ipAddress = String.format("%s.%s.%s.%s", ip01, ip02, ip03, ip04);
        internet.setIpAddress(ipAddress);
        String lat = tfLatitude.getText();
        if (lat != null && !lat.isEmpty()) {
            internet.setLatitude(Float.parseFloat(tfLatitude.getText()));
        }
        String lot = tfLongitude.getText();
        if (lot != null && !lot.isEmpty()) {
            internet.setLongitude(Float.parseFloat(tfLongitude.getText()));
        }
        String elev = tfElevation.getText();
        if (elev != null && !elev.isEmpty()) {
            internet.setElevation(Float.parseFloat(tfElevation.getText()));
        }
        return internet;
    }
    
    private boolean internetInfoValidated() {
        return cbDataPlan.getValue() != null &&
                !tfMonthlyPayment.getText().isEmpty() &&
                !tfIP01.getText().isEmpty() &&
                !tfIP02.getText().isEmpty() &&
                !tfIP03.getText().isEmpty() &&
                !tfIP04.getText().isEmpty();
    }
    
}
