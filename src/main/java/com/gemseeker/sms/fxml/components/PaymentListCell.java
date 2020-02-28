package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.Loader;
import com.gemseeker.sms.data.Payment;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;

/**
 *
 * @author gemini1991
 */
public class PaymentListCell extends ListCell<Payment> {
    
    @FXML private Label lblName;
    @FXML private Label lblAmount;
    @FXML private Label lblQuantity;
    private Pane pane;
    
    public PaymentListCell() {
        init();
    }
    
    private void init() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("payment_list_cell.fxml"));
            loader.setController(this);
            pane = loader.load();
            
//        Loader loader = Loader.getInstance();
//        pane = loader.load("fxml/components/payment_list_cell.fxml", this);
        } catch (IOException ex) {
            Logger.getLogger(PaymentListCell.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void updateItem(Payment payment, boolean empty) {
        super.updateItem(payment, empty);
        if (empty || payment == null) {
            setText(null);
            setGraphic(null);
        } else {
            lblName.setText(payment.getName());
            lblAmount.setText(payment.getTotalAmount() + "");
            lblQuantity.setText(payment.getQuantity() + "");
            
            setText(null);
            setGraphic(pane);
        }
    }

}
