package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Billing;
import java.util.Date;
import javafx.scene.control.TableCell;

/**
 *
 * @author gemini1991
 */
public class BillingDateTableCell extends TableCell<Billing, Date> {
    @Override
    protected void updateItem(Date date, boolean empty) {
        super.updateItem(date, empty);
        setGraphic(null);
        if (date == null || empty) {
            setText("");
        } else {
            setText(Utils.TABLE_DATE_FORMAT.format(date));
        }
    }
}
