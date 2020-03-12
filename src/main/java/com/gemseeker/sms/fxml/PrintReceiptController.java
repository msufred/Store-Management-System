package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Balance;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Payment;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gemini1991
 */
public class PrintReceiptController extends Controller {
    
    @FXML private HBox receiptGroup;
    
    @FXML private Label lblDate;
    @FXML private Label lblAccountNo;
    @FXML private Label lblName;
    @FXML private Label lblAddress;
    @FXML private Label lblDueDate;
    @FXML private Label lblBalance;
    @FXML private Label lblTax;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotal;
    @FXML private TableView<Payment> itemsTable;
    @FXML private TableColumn<Payment, String> colName;
    @FXML private TableColumn<Payment, Double> colPrice;
    @FXML private TableColumn<Payment, Integer> colQty;
    @FXML private TableColumn<Payment, Double> colTotal;
    
    // copy
    @FXML private Label lblDate1;
    @FXML private Label lblAccountNo1;
    @FXML private Label lblName1;
    @FXML private Label lblAddress1;
    @FXML private Label lblDueDate1;
    @FXML private Label lblTotal1;
    @FXML private Label lblBalance1;
    @FXML private Label lblTax1;
    @FXML private Label lblDiscount1;
    @FXML private TableView<Payment> itemsTable1;
    @FXML private TableColumn<Payment, String> colName1;
    @FXML private TableColumn<Payment, Double> colPrice1;
    @FXML private TableColumn<Payment, Integer> colQty1;
    @FXML private TableColumn<Payment, Double> colTotal1;
    
    @FXML private Button btnPrint;
    @FXML private Button btnCancel;
    
    private Stage stage;
    private Scene scene;
    
    private Billing billing;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itemsTable.setSelectionModel(null);
        itemsTable.setSelectionModel(null);
        
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        colName1.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice1.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colQty1.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal1.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        btnCancel.setOnAction(evt -> close());
        btnPrint.setOnAction(evt -> {
            close();
            doPrint();
        });
    }

    public void show(Billing billing) {
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Print Notice");
            stage.initModality(Modality.APPLICATION_MODAL);
            scene = new Scene(getContentPane());
            stage.setScene(scene);
        }
        receiptGroup.getTransforms().clear();
        stage.show();
        this.billing = billing;
        showDetails();
    }
    
    public void close() {
        if (stage != null) stage.close();
    }
    
    private void showDetails() {
        Account account = billing.getAccount();
        if (account != null) {
            lblAccountNo.setText(account.getAccountNumber());
            lblName.setText(String.format("%s %s", account.getFirstName(), account.getLastName()));
            lblAddress.setText(account.getAddress().toString());
            
            lblAccountNo1.setText(account.getAccountNumber());
            lblName1.setText(String.format("%s %s", account.getFirstName(), account.getLastName()));
            lblAddress1.setText(account.getAddress().toString());
            
            ArrayList<Balance> balances = account.getBalances();
            if (!balances.isEmpty()) {
                for (Balance b : balances) {
                    double tBalance = 0;
                    if (!b.isIsPaid()) {
                        tBalance += b.getAmount();
                    }
                    lblBalance.setText(tBalance + "");
                    lblBalance1.setText(tBalance + "");
                }
            }
        }
        
        lblDueDate.setText(billing.getDueDate());
        lblDueDate1.setText(billing.getDueDate());
        
        itemsTable.setItems(FXCollections.observableArrayList(billing.getPayments()));
        itemsTable1.setItems(FXCollections.observableArrayList(billing.getPayments()));
        
        lblTotal.setText(billing.getAmount() + "");
        lblTotal1.setText(billing.getAmount() + "");
        
        lblDate.setText(Utils.DATE_FORMAT_1.format(Calendar.getInstance().getTime()));
        lblDate1.setText(Utils.DATE_FORMAT_1.format(Calendar.getInstance().getTime()));
    }
    
    private void doPrint() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(stage)) {
            Printer printer = Printer.getDefaultPrinter();
            PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.HARDWARE_MINIMUM);

            double pw = pageLayout.getPrintableWidth();
            double ph = pageLayout.getPrintableHeight();
            double nw = receiptGroup.getBoundsInParent().getWidth();
            double nh = receiptGroup.getBoundsInParent().getHeight();
            double sx = pw/nw;
            double sy = ph/nh;
            
            receiptGroup.getTransforms().add(new Scale(sx, sy));
            
            boolean success = job.printPage(pageLayout, receiptGroup);
            if (success) job.endJob();
            else {
                System.out.println("Failed to print...");
            }
        }
    }
}
