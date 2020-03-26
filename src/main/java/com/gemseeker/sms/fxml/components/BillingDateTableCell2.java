package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Billing;
import java.text.ParseException;
import java.util.Calendar;
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
            setText(null);
        } else {
            try {
                Date date = Utils.DATE_FORMAT_2.parse(item);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                
                Calendar now = Calendar.getInstance();
                if (cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && 
                        cal.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) &&
                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                    setText("Today");
                } else {
                    setText(Utils.TABLE_DATE_FORMAT.format(date));
                }
            } catch (ParseException e) {
                System.err.println("Error parsing date.\n" + e);
                setText(item);
            }
        }
    }
}
