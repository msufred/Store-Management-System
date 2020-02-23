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
public class BillingDateTableCellFactory implements Callback<TableColumn<Billing, Date>, TableCell<Billing, Date>> {

    @Override
    public TableCell<Billing, Date> call(TableColumn<Billing, Date> p) {
        return new BillingDateTableCell();
    }

}
