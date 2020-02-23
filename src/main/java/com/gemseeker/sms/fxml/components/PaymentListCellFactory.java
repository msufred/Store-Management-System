package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Payment;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 *
 * @author gemini1991
 */
public class PaymentListCellFactory implements Callback<ListView<Payment>, ListCell<Payment>>{

    @Override
    public ListCell<Payment> call(ListView<Payment> p) {
        return new PaymentListCell();
    }

}
