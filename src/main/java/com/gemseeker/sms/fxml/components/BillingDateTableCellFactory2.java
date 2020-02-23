package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Billing;
import java.util.Date;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 *
 * @author gemini1991
 */
public class BillingDateTableCellFactory2 implements Callback<TableColumn<Billing, String>, TableCell<Billing, String>> {

    @Override
    public TableCell<Billing, String> call(TableColumn<Billing, String> p) {
        return new BillingDateTableCell2();
    }

}
