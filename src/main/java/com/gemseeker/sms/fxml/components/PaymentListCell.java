package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Payment;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

/**
 *
 * @author gemini1991
 */
public class PaymentListCell extends ListCell<Payment> {

    @Override
    protected void updateItem(Payment payment, boolean empty) {
        super.updateItem(payment, empty);
        if (empty || payment == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(String.format("%.2f -- %s (%d)", 
                    payment.getAmount(),
                    payment.getName(),
                    payment.getQuantity()));
        }
    }

}
