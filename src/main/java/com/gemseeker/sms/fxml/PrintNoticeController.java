package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Controller;
import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.Payment;
import java.net.URL;
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
public class PrintNoticeController extends Controller {

    @FXML private HBox noticeGroup;
    
    @FXML private Label lblDate;
    @FXML private Label lblAccountNo;
    @FXML private Label lblName;
    @FXML private Label lblAddress;
    @FXML private Label lblDueDate;
    @FXML private Label lblTotal;
    @FXML TableView<Payment> itemsTable;
    @FXML TableColumn<Payment, String> colName;
    @FXML TableColumn<Payment, Double> colPrice;
    @FXML TableColumn<Payment, Integer> colQty;
    @FXML TableColumn<Payment, Double> colTotal;
    
    // copy
    @FXML private Label lblDate1;
    @FXML private Label lblAccountNo1;
    @FXML private Label lblName1;
    @FXML private Label lblAddress1;
    @FXML private Label lblDueDate1;
    @FXML private Label lblTotal1;
    @FXML TableView<Payment> itemsTable1;
    @FXML TableColumn<Payment, String> colName1;
    @FXML TableColumn<Payment, Double> colPrice1;
    @FXML TableColumn<Payment, Integer> colQty1;
    @FXML TableColumn<Payment, Double> colTotal1;
    
    @FXML private Button btnPrint;
    @FXML private Button btnCancel;
    
    private Stage stage;
    private Scene scene;
    
    private final BillingsController billingsController;
    private Billing billing;
    
    public PrintNoticeController(BillingsController billingsController) {
        this.billingsController = billingsController;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
        noticeGroup.getTransforms().clear();
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
            double nw = noticeGroup.getBoundsInParent().getWidth();
            double nh = noticeGroup.getBoundsInParent().getHeight();
            double sx = pw/nw;
            double sy = ph/nh;
            
            noticeGroup.getTransforms().add(new Scale(sx, sy));
            
            boolean success = job.printPage(pageLayout, noticeGroup);
            if (success) job.endJob();
            else {
                System.out.println("Failed to print...");
            }
        }
    }
}
