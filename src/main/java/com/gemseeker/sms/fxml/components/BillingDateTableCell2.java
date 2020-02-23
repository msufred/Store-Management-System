package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Billing;
import java.text.ParseException;
import java.util.Date;
import javafx.scene.control.TableCell;

/**
 *
 * @author gemini1991
 */
public class BillingDateTableCell2 extends TableCell<Billing, String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
        if (item == null || empty) {
            setText("");
        } else {
            try {
                Date date = Utils.DATE_FORMAT_2.parse(item);
                setText(Utils.TABLE_DATE_FORMAT.format(date));
            } catch (ParseException e) {
                System.err.println("Error parsing date.\n" + e);
                setText(item);
            }
        }
    }
}
